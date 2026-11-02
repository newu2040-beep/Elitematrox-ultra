package com.example.data.remote

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EliteMatrixRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun saveAndObservePreset_authenticatedOwner_succeeds() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = EliteMatrixFirestoreRepository(firestore, auth)
        val presetId = "preset_${UUID.randomUUID().toString().replace("-", "").take(12)}"

        val preset = FirestoreVideoPreset(
            userId = uid,
            name = "Cinema 1080p",
            category = "enhance",
            resolution = "1080p",
            fps = 30L,
            codec = "H.264",
            bitrateMode = "Auto",
            bitrateKbps = 12000L,
            containerFormat = "MP4",
            crf = 18L,
            encoderPreset = "medium",
            brightness = 15L,
            contrast = 20L,
            saturation = 10L,
            temperature = 0L,
            sharpness = 25L,
            denoise = 10L
        )

        val saveResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.savePreset(preset, customId = presetId)
        }
        assertTrue("Expected preset save to succeed, got: ${saveResult.exceptionOrNull()}", saveResult.isSuccess)

        val emitted = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observePresets().first { list -> list.any { it.id == presetId } }
        }
        assertTrue(emitted.any { it.id == presetId && it.name == "Cinema 1080p" })
    }

    @Test
    fun createJobAndExportRecord_authenticatedOwner_succeeds() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = EliteMatrixFirestoreRepository(firestore, auth)
        val jobId = "job_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val exportId = "exp_${UUID.randomUUID().toString().replace("-", "").take(12)}"

        val job = FirestoreProcessingJob(
            userId = uid,
            sourceFileName = "Nature_Video.mp4",
            sourceUri = "file:///samples/Nature_Video.mp4",
            sourceSizeBytes = 25690112L,
            operationType = "enhance",
            executionRoute = "local",
            configSummary = "1080p • 30 FPS • H.264",
            status = "processing",
            progressPercent = 40L,
            outputSizeBytes = 0L
        )

        val jobRes = withTimeout(DEFAULT_TIMEOUT_MS) { repository.createJob(job, customId = jobId) }
        assertTrue(jobRes.isSuccess)

        val updateRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.updateJobProgress(
                jobId = jobId,
                status = "completed",
                progressPercent = 100,
                outputFilePath = "file:///exports/Nature_Video.mp4",
                outputSizeBytes = 19608371L
            )
        }
        assertTrue(updateRes.isSuccess)

        val exportRecord = FirestoreExportRecord(
            userId = uid,
            jobId = jobId,
            fileName = "Nature_Video.mp4",
            originalSizeBytes = 25690112L,
            outputSizeBytes = 19608371L,
            savedPercent = 24L,
            resolution = "1080p",
            fps = 30L,
            codec = "H.264",
            containerFormat = "MP4",
            durationMs = 15000L,
            localUri = "file:///exports/Nature_Video.mp4"
        )
        val expRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveExportRecord(exportRecord, customId = exportId)
        }
        assertTrue(expRes.isSuccess)
    }

    @Test
    fun getPresetById_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = EliteMatrixFirestoreRepository(firestore, auth)
        val presetId = "preset_${UUID.randomUUID().toString().replace("-", "").take(12)}"

        withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.savePreset(
                FirestoreVideoPreset(userId = aliceUid, name = "Alice Secret Preset"),
                customId = presetId
            ).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = EliteMatrixFirestoreRepository(firestore, auth)
        val bobAttempt = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.getPresetById(presetId, ownerUid = aliceUid)
        }
        assertTrue(bobAttempt.isFailure)
        val firestoreEx = bobAttempt.exceptionOrNull()?.unwrapFirestoreException()
        assertNotNull(firestoreEx)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx?.code)
    }

    @Test
    fun observePresets_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repository = EliteMatrixFirestoreRepository(firestore, auth)

        var caught: Throwable? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observePresets().first()
            }
        } catch (t: Throwable) {
            caught = t
        }
        assertNotNull("Expected exception for unauthenticated user", caught)
        val firestoreEx = caught?.unwrapFirestoreException()
        assertNotNull("Expected wrapped FirebaseFirestoreException, got $caught", firestoreEx)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx?.code)
    }

    private companion object {
        const val ALICE_EMAIL = "alice@elitematrix.test"
        const val BOB_EMAIL = "bob@elitematrix.test"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
