package com.example.data.remote

import android.content.Context
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class EliteMatrixFirestoreRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    // CRITICAL: Always initialize with R.string.firestore_database_id
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        Firebase.auth
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    // --- 1. User Profile (/users/{uid}) ---

    suspend fun upsertUserProfile(
        displayName: String,
        email: String,
        photoUrl: String? = null,
        cloudConsentGranted: Boolean = false
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        try {
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                val updates = buildMap<String, Any> {
                    put("displayName", displayName.ifBlank { "Creator" }.take(100))
                    put("email", email.take(254))
                    if (!photoUrl.isNullOrBlank()) put("photoUrl", photoUrl.take(1024))
                    put("cloudConsentGranted", cloudConsentGranted)
                    put("updatedAt", FieldValue.serverTimestamp())
                }
                docRef.update(updates).await()
            } else {
                val profile = FirestoreUserProfile(
                    id = uid,
                    userId = uid,
                    displayName = displayName.ifBlank { "Creator" },
                    email = email,
                    photoUrl = photoUrl,
                    accountTier = "pro",
                    cloudConsentGranted = cloudConsentGranted
                )
                docRef.set(profile.toCreateMap()).await()
            }
            uid
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun getUserProfile(targetUserId: String = requireUserId()): Result<FirestoreUserProfile?> = runCatching {
        val docRef = db.collection("users").document(targetUserId)
        try {
            val snap = docRef.get().await()
            snap.toObject(FirestoreUserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
    }

    // --- 2. Presets (/users/{uid}/presets/{presetId}) ---

    suspend fun savePreset(preset: FirestoreVideoPreset, customId: String? = null): Result<String> = runCatching {
        val uid = requireUserId()
        val presetId = customId?.takeIf { it.isNotBlank() }
            ?: preset.id.takeIf { it.isNotBlank() }
            ?: "preset_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val docRef = db.collection("users").document(uid).collection("presets").document(presetId)
        try {
            val payload = preset.copy(userId = uid).toCreateMap()
            docRef.set(payload).await()
            presetId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun getPresetById(presetId: String, ownerUid: String = requireUserId()): Result<FirestoreVideoPreset?> = runCatching {
        val docRef = db.collection("users").document(ownerUid).collection("presets").document(presetId)
        try {
            val snap = docRef.get().await()
            snap.toObject(FirestoreVideoPreset::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
    }

    suspend fun getUserPresets(): Result<List<FirestoreVideoPreset>> = runCatching {
        val uid = requireUserId()
        val colRef = db.collection("users").document(uid).collection("presets")
        try {
            val snap = colRef.whereEqualTo("userId", uid).get().await()
            snap.toObjects(FirestoreVideoPreset::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                .sortedByDescending { it.createdAt?.seconds ?: 0L }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, colRef.path)
            throw e
        }
    }

    fun observePresets(): Flow<List<FirestoreVideoPreset>> = flow {
        val uid = auth.currentUser?.uid ?: ""
        val colRef = db.collection("users").document(uid.ifBlank { "unauthenticated" }).collection("presets")
        emitAll(
            colRef.whereEqualTo("userId", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        FirestoreVideoPreset::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, colRef.path)
                    throw error
                }
        )
    }

    suspend fun deletePreset(presetId: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("presets").document(presetId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- 3. Processing Jobs (/users/{uid}/jobs/{jobId}) ---

    suspend fun createJob(job: FirestoreProcessingJob, customId: String? = null): Result<String> = runCatching {
        val uid = requireUserId()
        val jobId = customId?.takeIf { it.isNotBlank() }
            ?: job.id.takeIf { it.isNotBlank() }
            ?: "job_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val docRef = db.collection("users").document(uid).collection("jobs").document(jobId)
        try {
            docRef.set(job.copy(userId = uid).toCreateMap()).await()
            jobId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateJobProgress(
        jobId: String,
        status: String,
        progressPercent: Int,
        outputFilePath: String? = null,
        outputSizeBytes: Long? = null,
        errorCode: String? = null,
        errorMessage: String? = null
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("jobs").document(jobId)
        try {
            val updates = buildMap<String, Any> {
                put("status", status)
                put("progressPercent", progressPercent.toLong().coerceIn(0L, 100L))
                if (!outputFilePath.isNullOrBlank()) put("outputFilePath", outputFilePath.take(2048))
                if (outputSizeBytes != null) put("outputSizeBytes", outputSizeBytes.coerceAtLeast(0L))
                if (!errorCode.isNullOrBlank()) put("errorCode", errorCode.take(64))
                if (!errorMessage.isNullOrBlank()) put("errorMessage", errorMessage.take(500))
                put("updatedAt", FieldValue.serverTimestamp())
            }
            docRef.update(updates).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    fun observeJobs(): Flow<List<FirestoreProcessingJob>> = flow {
        val uid = auth.currentUser?.uid ?: ""
        val colRef = db.collection("users").document(uid.ifBlank { "unauthenticated" }).collection("jobs")
        emitAll(
            colRef.whereEqualTo("userId", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        FirestoreProcessingJob::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, colRef.path)
                    throw error
                }
        )
    }

    // --- 4. Export Records (/users/{uid}/exports/{exportId}) ---

    suspend fun saveExportRecord(exportRecord: FirestoreExportRecord, customId: String? = null): Result<String> = runCatching {
        val uid = requireUserId()
        val exportId = customId?.takeIf { it.isNotBlank() }
            ?: exportRecord.id.takeIf { it.isNotBlank() }
            ?: "exp_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val docRef = db.collection("users").document(uid).collection("exports").document(exportId)
        try {
            docRef.set(exportRecord.copy(userId = uid).toCreateMap()).await()
            exportId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    fun observeExports(): Flow<List<FirestoreExportRecord>> = flow {
        val uid = auth.currentUser?.uid ?: ""
        val colRef = db.collection("users").document(uid.ifBlank { "unauthenticated" }).collection("exports")
        emitAll(
            colRef.whereEqualTo("userId", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        FirestoreExportRecord::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, colRef.path)
                    throw error
                }
        )
    }

    suspend fun deleteExportRecord(exportId: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("exports").document(exportId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- 5. User Settings (/users/{uid}/settings/preferences) ---

    suspend fun savePreferences(prefs: FirestoreUserPreferences): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("settings").document("preferences")
        try {
            val snap = docRef.get().await()
            if (snap.exists()) {
                val updates = mapOf(
                    "themePalette" to prefs.themePalette,
                    "themeMode" to prefs.themeMode,
                    "defaultRouting" to prefs.defaultRouting,
                    "cloudConsentAskEveryTime" to prefs.cloudConsentAskEveryTime,
                    "preserveMetadata" to prefs.preserveMetadata,
                    "notificationsEnabled" to prefs.notificationsEnabled,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updates).await()
            } else {
                docRef.set(prefs.copy(userId = uid).toCreateMap()).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }
}
