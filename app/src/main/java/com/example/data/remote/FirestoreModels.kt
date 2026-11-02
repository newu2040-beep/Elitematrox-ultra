package com.example.data.remote

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class FirestoreUserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "Creator",
    val email: String = "",
    val photoUrl: String? = null,
    val accountTier: String = "pro",
    val cloudConsentGranted: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = buildMap {
        put("userId", userId)
        put("displayName", displayName.ifBlank { "Creator" }.take(100))
        put("email", email.take(254))
        if (!photoUrl.isNullOrBlank()) put("photoUrl", photoUrl.take(1024))
        put("accountTier", if (accountTier in listOf("standard", "pro")) accountTier else "pro")
        put("cloudConsentGranted", cloudConsentGranted)
        put("createdAt", createdAt ?: FieldValue.serverTimestamp())
        put("updatedAt", FieldValue.serverTimestamp())
    }
}

@IgnoreExtraProperties
data class FirestoreVideoPreset(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val category: String = "custom",
    val resolution: String = "1080p",
    val fps: Long = 30L,
    val codec: String = "H.264",
    val bitrateMode: String = "Auto",
    val bitrateKbps: Long = 10000L,
    val containerFormat: String = "MP4",
    val crf: Long = 20L,
    val encoderPreset: String = "medium",
    val brightness: Long = 0L,
    val contrast: Long = 0L,
    val saturation: Long = 0L,
    val temperature: Long = 0L,
    val sharpness: Long = 0L,
    val denoise: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "name" to name.ifBlank { "Custom Preset" }.take(80),
        "category" to if (category in listOf("enhance", "compress", "hq_encoder", "social", "custom")) category else "custom",
        "resolution" to if (resolution in listOf("Original", "720p", "1080p", "2K", "4K")) resolution else "1080p",
        "fps" to if (fps in listOf(24L, 30L, 60L)) fps else 30L,
        "codec" to if (codec in listOf("H.264", "H.265", "AV1")) codec else "H.264",
        "bitrateMode" to if (bitrateMode in listOf("Auto", "Custom")) bitrateMode else "Auto",
        "bitrateKbps" to bitrateKbps.coerceIn(100L, 200000L),
        "containerFormat" to if (containerFormat in listOf("MP4", "MOV", "MKV")) containerFormat else "MP4",
        "crf" to crf.coerceIn(0L, 51L),
        "encoderPreset" to encoderPreset.ifBlank { "medium" }.take(32),
        "brightness" to brightness.coerceIn(-100L, 100L),
        "contrast" to contrast.coerceIn(-100L, 100L),
        "saturation" to saturation.coerceIn(-100L, 100L),
        "temperature" to temperature.coerceIn(-100L, 100L),
        "sharpness" to sharpness.coerceIn(0L, 100L),
        "denoise" to denoise.coerceIn(0L, 100L),
        "createdAt" to (createdAt ?: FieldValue.serverTimestamp()),
        "updatedAt" to FieldValue.serverTimestamp()
    )
}

@IgnoreExtraProperties
data class FirestoreProcessingJob(
    @DocumentId val id: String = "",
    val userId: String = "",
    val sourceFileName: String = "",
    val sourceUri: String = "",
    val sourceSizeBytes: Long = 0L,
    val operationType: String = "enhance",
    val executionRoute: String = "local",
    val configSummary: String = "",
    val status: String = "waiting",
    val progressPercent: Long = 0L,
    val errorCode: String? = null,
    val errorMessage: String? = null,
    val outputFilePath: String? = null,
    val outputSizeBytes: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = buildMap {
        put("userId", userId)
        put("sourceFileName", sourceFileName.ifBlank { "video.mp4" }.take(255))
        put("sourceUri", sourceUri.ifBlank { "file:///video.mp4" }.take(2048))
        put("sourceSizeBytes", sourceSizeBytes.coerceAtLeast(0L))
        put("operationType", if (operationType in listOf("enhance", "compress", "hq_encode", "custom_export", "convert")) operationType else "enhance")
        put("executionRoute", if (executionRoute in listOf("local", "cloud")) executionRoute else "local")
        put("configSummary", configSummary.ifBlank { "1080p • 30 FPS • H.264" }.take(500))
        put("status", if (status in listOf("waiting", "uploading", "queued", "processing", "downloading", "completed", "failed", "cancelled")) status else "waiting")
        put("progressPercent", progressPercent.coerceIn(0L, 100L))
        if (!errorCode.isNullOrBlank()) put("errorCode", errorCode.take(64))
        if (!errorMessage.isNullOrBlank()) put("errorMessage", errorMessage.take(500))
        if (!outputFilePath.isNullOrBlank()) put("outputFilePath", outputFilePath.take(2048))
        put("outputSizeBytes", outputSizeBytes.coerceAtLeast(0L))
        put("createdAt", createdAt ?: FieldValue.serverTimestamp())
        put("updatedAt", FieldValue.serverTimestamp())
    }
}

@IgnoreExtraProperties
data class FirestoreExportRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val jobId: String = "",
    val fileName: String = "",
    val originalSizeBytes: Long = 0L,
    val outputSizeBytes: Long = 0L,
    val savedPercent: Long = 0L,
    val resolution: String = "1080p",
    val fps: Long = 30L,
    val codec: String = "H.264",
    val containerFormat: String = "MP4",
    val durationMs: Long = 0L,
    val localUri: String = "",
    val cloudObjectRef: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = buildMap {
        put("userId", userId)
        put("jobId", jobId.ifBlank { "job_default" }.take(128))
        put("fileName", fileName.ifBlank { "Export_Video.mp4" }.take(255))
        put("originalSizeBytes", originalSizeBytes.coerceAtLeast(0L))
        put("outputSizeBytes", outputSizeBytes.coerceAtLeast(0L))
        put("savedPercent", savedPercent.coerceIn(-1000L, 100L))
        put("resolution", resolution.ifBlank { "1080p" }.take(32))
        put("fps", fps.coerceIn(1L, 240L))
        put("codec", codec.ifBlank { "H.264" }.take(32))
        put("containerFormat", if (containerFormat in listOf("MP4", "MOV", "MKV")) containerFormat else "MP4")
        put("durationMs", durationMs.coerceAtLeast(0L))
        put("localUri", localUri.ifBlank { "file:///Export_Video.mp4" }.take(2048))
        if (!cloudObjectRef.isNullOrBlank()) put("cloudObjectRef", cloudObjectRef.take(1024))
        put("createdAt", createdAt ?: FieldValue.serverTimestamp())
        put("updatedAt", FieldValue.serverTimestamp())
    }
}

@IgnoreExtraProperties
data class FirestoreUserPreferences(
    @DocumentId val id: String = "preferences",
    val userId: String = "",
    val themePalette: String = "WarmIvory",
    val themeMode: String = "Light",
    val defaultRouting: String = "Auto",
    val cloudConsentAskEveryTime: Boolean = true,
    val preserveMetadata: Boolean = true,
    val notificationsEnabled: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "themePalette" to if (themePalette in listOf("WarmIvory", "SoftMint", "Cream", "Lavender", "Sky", "Peach", "Sand", "Graphite", "Midnight")) themePalette else "WarmIvory",
        "themeMode" to if (themeMode in listOf("Light", "Dark", "System")) themeMode else "Light",
        "defaultRouting" to if (defaultRouting in listOf("Auto", "Local", "Cloud")) defaultRouting else "Auto",
        "cloudConsentAskEveryTime" to cloudConsentAskEveryTime,
        "preserveMetadata" to preserveMetadata,
        "notificationsEnabled" to notificationsEnabled,
        "createdAt" to (createdAt ?: FieldValue.serverTimestamp()),
        "updatedAt" to FieldValue.serverTimestamp()
    )
}
