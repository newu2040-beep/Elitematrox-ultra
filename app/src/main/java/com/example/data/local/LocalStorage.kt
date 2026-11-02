package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.domain.model.AppFontFamily
import com.example.domain.model.ExecutionRoute
import com.example.domain.model.ExportItem
import com.example.domain.model.JobItem
import com.example.domain.model.PresetItem
import com.example.domain.model.ThemeMode
import com.example.domain.model.ThemePalette
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.eliteMatrixDataStore by preferencesDataStore(name = "elite_matrix_preferences")

data class AppPreferencesState(
    val themePalette: ThemePalette = ThemePalette.WarmIvory,
    val themeMode: ThemeMode = ThemeMode.Light,
    val fontFamily: AppFontFamily = AppFontFamily.PlusJakarta,
    val defaultRoute: ExecutionRoute = ExecutionRoute.Auto,
    val cloudConsentGranted: Boolean = false,
    val askConsentEveryTime: Boolean = true,
    val preserveMetadata: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val isGuestMode: Boolean = false,
    val cloudRunEndpointUrl: String = "https://elite-matrix-worker-117042699383.asia-southeast1.run.app"
)

class PreferencesDataSource(private val context: Context) {
    private object Keys {
        val THEME_PALETTE = stringPreferencesKey("theme_palette")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val FONT_FAMILY = stringPreferencesKey("font_family")
        val DEFAULT_ROUTE = stringPreferencesKey("default_route")
        val CLOUD_CONSENT = booleanPreferencesKey("cloud_consent_granted")
        val ASK_CONSENT_EVERY_TIME = booleanPreferencesKey("ask_consent_every_time")
        val PRESERVE_METADATA = booleanPreferencesKey("preserve_metadata")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val IS_GUEST_MODE = booleanPreferencesKey("is_guest_mode")
        val CLOUD_RUN_URL = stringPreferencesKey("cloud_run_url")
    }

    val preferencesFlow: Flow<AppPreferencesState> = context.eliteMatrixDataStore.data.map { prefs ->
        val palette = prefs[Keys.THEME_PALETTE]?.let {
            runCatching { ThemePalette.valueOf(it) }.getOrNull()
        } ?: ThemePalette.WarmIvory
        val mode = prefs[Keys.THEME_MODE]?.let {
            runCatching { ThemeMode.valueOf(it) }.getOrNull()
        } ?: ThemeMode.Light
        val font = prefs[Keys.FONT_FAMILY]?.let {
            runCatching { AppFontFamily.valueOf(it) }.getOrNull()
        } ?: AppFontFamily.PlusJakarta
        val route = prefs[Keys.DEFAULT_ROUTE]?.let {
            runCatching { ExecutionRoute.valueOf(it) }.getOrNull()
        } ?: ExecutionRoute.Auto

        AppPreferencesState(
            themePalette = palette,
            themeMode = mode,
            fontFamily = font,
            defaultRoute = route,
            cloudConsentGranted = prefs[Keys.CLOUD_CONSENT] ?: false,
            askConsentEveryTime = prefs[Keys.ASK_CONSENT_EVERY_TIME] ?: true,
            preserveMetadata = prefs[Keys.PRESERVE_METADATA] ?: true,
            notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true,
            isGuestMode = prefs[Keys.IS_GUEST_MODE] ?: false,
            cloudRunEndpointUrl = prefs[Keys.CLOUD_RUN_URL]
                ?: "https://elite-matrix-worker-117042699383.asia-southeast1.run.app"
        )
    }

    suspend fun updateThemePalette(palette: ThemePalette) {
        context.eliteMatrixDataStore.edit { it[Keys.THEME_PALETTE] = palette.name }
    }

    suspend fun updateThemeMode(mode: ThemeMode) {
        context.eliteMatrixDataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun updateFontFamily(fontFamily: AppFontFamily) {
        context.eliteMatrixDataStore.edit { it[Keys.FONT_FAMILY] = fontFamily.name }
    }

    suspend fun updateDefaultRoute(route: ExecutionRoute) {
        context.eliteMatrixDataStore.edit { it[Keys.DEFAULT_ROUTE] = route.name }
    }

    suspend fun updateCloudConsent(granted: Boolean, askEveryTime: Boolean) {
        context.eliteMatrixDataStore.edit {
            it[Keys.CLOUD_CONSENT] = granted
            it[Keys.ASK_CONSENT_EVERY_TIME] = askEveryTime
        }
    }

    suspend fun updatePreserveMetadata(preserve: Boolean) {
        context.eliteMatrixDataStore.edit { it[Keys.PRESERVE_METADATA] = preserve }
    }

    suspend fun updateNotificationsEnabled(enabled: Boolean) {
        context.eliteMatrixDataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setGuestMode(isGuest: Boolean) {
        context.eliteMatrixDataStore.edit { it[Keys.IS_GUEST_MODE] = isGuest }
    }
}

@Entity(tableName = "local_presets")
data class LocalPresetEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val resolution: String,
    val fps: Int,
    val codec: String,
    val bitrateMode: String,
    val bitrateKbps: Int,
    val containerFormat: String,
    val crf: Int,
    val encoderPreset: String,
    val brightness: Int,
    val contrast: Int,
    val saturation: Int,
    val temperature: Int,
    val sharpness: Int,
    val denoise: Int,
    val updatedAtMillis: Long
) {
    fun toDomain(): PresetItem = PresetItem(
        id = id,
        name = name,
        category = category,
        resolution = resolution,
        fps = fps,
        codec = codec,
        bitrateMode = bitrateMode,
        bitrateKbps = bitrateKbps,
        containerFormat = containerFormat,
        crf = crf,
        encoderPreset = encoderPreset,
        brightness = brightness,
        contrast = contrast,
        saturation = saturation,
        temperature = temperature,
        sharpness = sharpness,
        denoise = denoise,
        isBuiltIn = false,
        updatedAtMillis = updatedAtMillis
    )

    companion object {
        fun fromDomain(item: PresetItem): LocalPresetEntity = LocalPresetEntity(
            id = item.id,
            name = item.name,
            category = item.category,
            resolution = item.resolution,
            fps = item.fps,
            codec = item.codec,
            bitrateMode = item.bitrateMode,
            bitrateKbps = item.bitrateKbps,
            containerFormat = item.containerFormat,
            crf = item.crf,
            encoderPreset = item.encoderPreset,
            brightness = item.brightness,
            contrast = item.contrast,
            saturation = item.saturation,
            temperature = item.temperature,
            sharpness = item.sharpness,
            denoise = item.denoise,
            updatedAtMillis = item.updatedAtMillis
        )
    }
}

@Entity(tableName = "local_jobs")
data class LocalJobEntity(
    @PrimaryKey val id: String,
    val sourceFileName: String,
    val sourceUri: String,
    val sourceSizeBytes: Long,
    val operationType: String,
    val executionRoute: String,
    val configSummary: String,
    val status: String,
    val progressPercent: Int,
    val errorCode: String?,
    val errorMessage: String?,
    val outputFilePath: String?,
    val outputSizeBytes: Long,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
) {
    fun toDomain(): JobItem = JobItem(
        id = id,
        sourceFileName = sourceFileName,
        sourceUri = sourceUri,
        sourceSizeBytes = sourceSizeBytes,
        operationType = operationType,
        executionRoute = executionRoute,
        configSummary = configSummary,
        status = status,
        progressPercent = progressPercent,
        errorCode = errorCode,
        errorMessage = errorMessage,
        outputFilePath = outputFilePath,
        outputSizeBytes = outputSizeBytes,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis
    )

    companion object {
        fun fromDomain(item: JobItem): LocalJobEntity = LocalJobEntity(
            id = item.id,
            sourceFileName = item.sourceFileName,
            sourceUri = item.sourceUri,
            sourceSizeBytes = item.sourceSizeBytes,
            operationType = item.operationType,
            executionRoute = item.executionRoute,
            configSummary = item.configSummary,
            status = item.status,
            progressPercent = item.progressPercent,
            errorCode = item.errorCode,
            errorMessage = item.errorMessage,
            outputFilePath = item.outputFilePath,
            outputSizeBytes = item.outputSizeBytes,
            createdAtMillis = item.createdAtMillis,
            updatedAtMillis = item.updatedAtMillis
        )
    }
}

@Entity(tableName = "local_exports")
data class LocalExportEntity(
    @PrimaryKey val id: String,
    val jobId: String,
    val fileName: String,
    val originalSizeBytes: Long,
    val outputSizeBytes: Long,
    val savedPercent: Int,
    val resolution: String,
    val fps: Int,
    val codec: String,
    val containerFormat: String,
    val durationMs: Long,
    val localUri: String,
    val cloudObjectRef: String?,
    val createdAtMillis: Long
) {
    fun toDomain(): ExportItem = ExportItem(
        id = id,
        jobId = jobId,
        fileName = fileName,
        originalSizeBytes = originalSizeBytes,
        outputSizeBytes = outputSizeBytes,
        savedPercent = savedPercent,
        resolution = resolution,
        fps = fps,
        codec = codec,
        containerFormat = containerFormat,
        durationMs = durationMs,
        localUri = localUri,
        cloudObjectRef = cloudObjectRef,
        createdAtMillis = createdAtMillis
    )

    companion object {
        fun fromDomain(item: ExportItem): LocalExportEntity = LocalExportEntity(
            id = item.id,
            jobId = item.jobId,
            fileName = item.fileName,
            originalSizeBytes = item.originalSizeBytes,
            outputSizeBytes = item.outputSizeBytes,
            savedPercent = item.savedPercent,
            resolution = item.resolution,
            fps = item.fps,
            codec = item.codec,
            containerFormat = item.containerFormat,
            durationMs = item.durationMs,
            localUri = item.localUri,
            cloudObjectRef = item.cloudObjectRef,
            createdAtMillis = item.createdAtMillis
        )
    }
}

@Dao
interface EliteMatrixDao {
    @Query("SELECT * FROM local_presets ORDER BY updatedAtMillis DESC")
    fun observePresets(): Flow<List<LocalPresetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPreset(preset: LocalPresetEntity)

    @Query("DELETE FROM local_presets WHERE id = :id")
    suspend fun deletePreset(id: String)

    @Query("SELECT * FROM local_jobs ORDER BY createdAtMillis DESC")
    fun observeJobs(): Flow<List<LocalJobEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertJob(job: LocalJobEntity)

    @Query("DELETE FROM local_jobs WHERE id = :id")
    suspend fun deleteJob(id: String)

    @Query("SELECT * FROM local_exports ORDER BY createdAtMillis DESC")
    fun observeExports(): Flow<List<LocalExportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertExport(export: LocalExportEntity)

    @Query("DELETE FROM local_exports WHERE id = :id")
    suspend fun deleteExport(id: String)

    @Query("DELETE FROM local_presets")
    suspend fun clearAllPresets()

    @Query("DELETE FROM local_jobs")
    suspend fun clearAllJobs()

    @Query("DELETE FROM local_exports")
    suspend fun clearAllExports()
}

@Database(
    entities = [LocalPresetEntity::class, LocalJobEntity::class, LocalExportEntity::class],
    version = 1,
    exportSchema = false
)
abstract class EliteMatrixDatabase : RoomDatabase() {
    abstract fun dao(): EliteMatrixDao

    companion object {
        @Volatile
        private var INSTANCE: EliteMatrixDatabase? = null

        fun getInstance(context: Context): EliteMatrixDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    EliteMatrixDatabase::class.java,
                    "elite_matrix_room.db"
                ).fallbackToDestructiveMigration(true).build().also { INSTANCE = it }
            }
        }
    }
}
