package com.example.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.R
import com.example.core.media.ProcessedVideoOutput
import com.example.core.media.VideoProcessingEngine
import com.example.core.network.CloudWorkerClient
import com.example.data.local.AppPreferencesState
import com.example.data.local.EliteMatrixDao
import com.example.data.local.EliteMatrixDatabase
import com.example.data.local.LocalExportEntity
import com.example.data.local.LocalJobEntity
import com.example.data.local.LocalPresetEntity
import com.example.data.local.PreferencesDataSource
import com.example.data.remote.EliteMatrixFirestoreRepository
import com.example.data.remote.FirestoreExportRecord
import com.example.data.remote.FirestoreProcessingJob
import com.example.data.remote.FirestoreUserPreferences
import com.example.data.remote.FirestoreVideoPreset
import com.example.domain.model.BuiltInPresets
import com.example.domain.model.CompressionMode
import com.example.domain.model.EnhancementAdjustments
import com.example.domain.model.ExecutionRoute
import com.example.domain.model.ExportConfiguration
import com.example.domain.model.ExportItem
import com.example.domain.model.JobItem
import com.example.domain.model.PresetItem
import com.example.domain.model.SocialMediaPreset
import com.example.domain.model.ThemeMode
import com.example.domain.model.ThemePalette
import com.example.domain.model.VideoMetadata
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PrimaryScreen {
    Home,
    EnhanceVideo,
    ExportSettings,
    ProcessingComplete,
    ToolsHub,
    ProjectsHistory,
    SettingsScreen
}

enum class ActiveToolModal {
    None,
    VideoInspector,
    PresetManager,
    FormatConverter,
    BitrateCalculator,
    CompressWorkflow,
    HqEncoderWorkflow
}

data class EditorUiState(
    val activeScreen: PrimaryScreen = PrimaryScreen.Home,
    val activeToolModal: ActiveToolModal = ActiveToolModal.None,
    val selectedVideo: VideoMetadata? = null,
    val videoThumbnail: Bitmap? = null,
    val adjustments: EnhancementAdjustments = EnhancementAdjustments(),
    val exportConfig: ExportConfiguration = ExportConfiguration(),
    val isProcessing: Boolean = false,
    val processingStatus: String = "Idle",
    val processingProgress: Int = 0,
    val lastCompletedOutput: ProcessedVideoOutput? = null,
    val statusMessage: String? = null,
    val showCloudConsentDialog: Boolean = false,
    val showVideoPlayerDialog: Boolean = false,
    val playbackUriString: String? = null
)

class EliteMatrixViewModel(
    private val preferencesDataSource: PreferencesDataSource,
    private val localDao: EliteMatrixDao,
    private val firestoreRepository: EliteMatrixFirestoreRepository,
    private val authenticatedUserId: String?
) : ViewModel() {

    private val _editorState = MutableStateFlow(EditorUiState())
    val editorState: StateFlow<EditorUiState> = _editorState.asStateFlow()

    private var activeProcessingJob: Job? = null

    val preferencesState: StateFlow<AppPreferencesState> = preferencesDataSource.preferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = AppPreferencesState()
        )

    // Combine local Room presets with built-in HQ presets (and Firestore presets when authenticated)
    private val remotePresetsFlow = if (authenticatedUserId != null) {
        firestoreRepository.observePresets()
            .catch { e ->
                Log.w("EliteMatrixVM", "Error observing remote presets", e)
                emit(emptyList())
            }
    } else {
        MutableStateFlow(emptyList())
    }

    val presetsState: StateFlow<List<PresetItem>> = combine(
        localDao.observePresets(),
        remotePresetsFlow
    ) { localEntities, remoteList ->
        val localItems = localEntities.map { it.toDomain() }
        val remoteItems = remoteList.map { r ->
            PresetItem(
                id = r.id,
                name = r.name,
                category = r.category,
                resolution = r.resolution,
                fps = r.fps.toInt(),
                codec = r.codec,
                bitrateMode = r.bitrateMode,
                bitrateKbps = r.bitrateKbps.toInt(),
                containerFormat = r.containerFormat,
                crf = r.crf.toInt(),
                encoderPreset = r.encoderPreset,
                brightness = r.brightness.toInt(),
                contrast = r.contrast.toInt(),
                saturation = r.saturation.toInt(),
                temperature = r.temperature.toInt(),
                sharpness = r.sharpness.toInt(),
                denoise = r.denoise.toInt(),
                isBuiltIn = false,
                updatedAtMillis = (r.updatedAt?.seconds ?: 0L) * 1000L
            )
        }
        val mergedUserPresets = (remoteItems + localItems).distinctBy { it.id }
        mergedUserPresets + BuiltInPresets.defaultHqPresets
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = BuiltInPresets.defaultHqPresets
    )

    private val remoteJobsFlow = if (authenticatedUserId != null) {
        firestoreRepository.observeJobs()
            .catch { e ->
                Log.w("EliteMatrixVM", "Error observing remote jobs", e)
                emit(emptyList())
            }
    } else {
        MutableStateFlow(emptyList())
    }

    val jobsState: StateFlow<List<JobItem>> = combine(
        localDao.observeJobs(),
        remoteJobsFlow
    ) { localEntities, remoteList ->
        val localItems = localEntities.map { it.toDomain() }
        val remoteItems = remoteList.map { r ->
            JobItem(
                id = r.id,
                sourceFileName = r.sourceFileName,
                sourceUri = r.sourceUri,
                sourceSizeBytes = r.sourceSizeBytes,
                operationType = r.operationType,
                executionRoute = r.executionRoute,
                configSummary = r.configSummary,
                status = r.status,
                progressPercent = r.progressPercent.toInt(),
                errorCode = r.errorCode,
                errorMessage = r.errorMessage,
                outputFilePath = r.outputFilePath,
                outputSizeBytes = r.outputSizeBytes,
                createdAtMillis = (r.createdAt?.seconds ?: 0L) * 1000L,
                updatedAtMillis = (r.updatedAt?.seconds ?: 0L) * 1000L
            )
        }
        (localItems + remoteItems)
            .distinctBy { it.id }
            .sortedByDescending { it.createdAtMillis }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptyList()
    )

    private val remoteExportsFlow = if (authenticatedUserId != null) {
        firestoreRepository.observeExports()
            .catch { e ->
                Log.w("EliteMatrixVM", "Error observing remote exports", e)
                emit(emptyList())
            }
    } else {
        MutableStateFlow(emptyList())
    }

    val exportsState: StateFlow<List<ExportItem>> = combine(
        localDao.observeExports(),
        remoteExportsFlow
    ) { localEntities, remoteList ->
        val localItems = localEntities.map { it.toDomain() }
        val remoteItems = remoteList.map { r ->
            ExportItem(
                id = r.id,
                jobId = r.jobId,
                fileName = r.fileName,
                originalSizeBytes = r.originalSizeBytes,
                outputSizeBytes = r.outputSizeBytes,
                savedPercent = r.savedPercent.toInt(),
                resolution = r.resolution,
                fps = r.fps.toInt(),
                codec = r.codec,
                containerFormat = r.containerFormat,
                durationMs = r.durationMs,
                localUri = r.localUri,
                cloudObjectRef = r.cloudObjectRef,
                createdAtMillis = (r.createdAt?.seconds ?: 0L) * 1000L
            )
        }
        (localItems + remoteItems)
            .distinctBy { it.id }
            .sortedByDescending { it.createdAtMillis }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptyList()
    )

    init {
        if (authenticatedUserId != null) {
            viewModelScope.launch {
                val user = Firebase.auth.currentUser
                if (user != null) {
                    firestoreRepository.upsertUserProfile(
                        displayName = user.displayName ?: "Creator",
                        email = user.email ?: "",
                        photoUrl = user.photoUrl?.toString(),
                        cloudConsentGranted = preferencesState.value.cloudConsentGranted
                    )
                }
            }
        }
    }

    fun navigateTo(screen: PrimaryScreen) {
        _editorState.update { it.copy(activeScreen = screen, statusMessage = null) }
    }

    fun openToolModal(modal: ActiveToolModal) {
        _editorState.update { it.copy(activeToolModal = modal, statusMessage = null) }
    }

    fun clearStatusMessage() {
        _editorState.update { it.copy(statusMessage = null) }
    }

    fun openVideoPlayer(uriString: String) {
        _editorState.update {
            it.copy(showVideoPlayerDialog = true, playbackUriString = uriString)
        }
    }

    fun closeVideoPlayer() {
        _editorState.update {
            it.copy(showVideoPlayerDialog = false, playbackUriString = null)
        }
    }

    fun importVideoUri(context: Context, uri: Uri, targetScreenAfterImport: PrimaryScreen? = null) {
        viewModelScope.launch {
            val inspected = VideoProcessingEngine.inspectVideo(context, uri)
            inspected.onSuccess { meta ->
                val thumb = VideoProcessingEngine.extractThumbnail(context, uri)
                _editorState.update { state ->
                    state.copy(
                        selectedVideo = meta,
                        videoThumbnail = thumb,
                        activeScreen = targetScreenAfterImport ?: state.activeScreen,
                        statusMessage = "Loaded ${meta.fileName} (${meta.resolutionLabel} • ${formatBytes(meta.sizeBytes)})"
                    )
                }
            }.onFailure { err ->
                _editorState.update {
                    it.copy(statusMessage = "Failed to inspect video: ${err.localizedMessage ?: "Unsupported or corrupt media"}")
                }
            }
        }
    }

    fun loadOrGenerateSampleVideo(context: Context, targetScreen: PrimaryScreen? = null) {
        viewModelScope.launch {
            _editorState.update { it.copy(statusMessage = "Preparing real H.264 sample video (Nature_Video.mp4)...") }
            val result = VideoProcessingEngine.generateRealSampleVideoMp4(context, "Nature_Video.mp4")
            result.onSuccess { uri ->
                val meta = VideoProcessingEngine.inspectVideo(context, uri).getOrNull()
                val thumb = VideoProcessingEngine.extractThumbnail(context, uri)
                _editorState.update { state ->
                    state.copy(
                        selectedVideo = meta,
                        videoThumbnail = thumb,
                        activeScreen = targetScreen ?: state.activeScreen,
                        statusMessage = "Loaded Nature_Video.mp4 (${meta?.resolutionLabel ?: "720p"})"
                    )
                }
            }.onFailure { err ->
                _editorState.update {
                    it.copy(statusMessage = "Could not generate sample clip: ${err.localizedMessage}")
                }
            }
        }
    }

    fun updateAdjustments(transform: (EnhancementAdjustments) -> EnhancementAdjustments) {
        _editorState.update { it.copy(adjustments = transform(it.adjustments)) }
    }

    fun resetAdjustments() {
        _editorState.update { it.copy(adjustments = EnhancementAdjustments.Neutral) }
    }

    fun updateExportConfig(transform: (ExportConfiguration) -> ExportConfiguration) {
        _editorState.update { it.copy(exportConfig = transform(it.exportConfig)) }
    }

    fun applySocialPreset(preset: SocialMediaPreset) {
        _editorState.update { state ->
            state.copy(
                exportConfig = state.exportConfig.copy(
                    selectedSocialPreset = preset,
                    resolution = preset.resolution,
                    fps = preset.fps,
                    codec = preset.codec,
                    bitrateMode = "Custom",
                    customBitrateKbps = preset.bitrateKbps,
                    containerFormat = preset.containerFormat
                ),
                statusMessage = "Applied ${preset.title} recommended profile (${preset.subtitle})"
            )
        }
    }

    fun applyPresetItem(preset: PresetItem) {
        _editorState.update { state ->
            state.copy(
                adjustments = state.adjustments.copy(
                    brightness = preset.brightness,
                    contrast = preset.contrast,
                    saturation = preset.saturation,
                    temperature = preset.temperature,
                    sharpness = preset.sharpness,
                    denoise = preset.denoise
                ),
                exportConfig = state.exportConfig.copy(
                    resolution = preset.resolution,
                    fps = preset.fps,
                    codec = preset.codec,
                    bitrateMode = preset.bitrateMode,
                    customBitrateKbps = preset.bitrateKbps,
                    containerFormat = preset.containerFormat,
                    crf = preset.crf,
                    encoderPreset = preset.encoderPreset,
                    hqEncoderPresetName = preset.name
                ),
                statusMessage = "Applied preset '${preset.name}'"
            )
        }
    }

    fun saveCurrentAsPreset(name: String, category: String = "custom") {
        val cleanName = name.trim().ifBlank { "Custom Preset" }
        val adj = _editorState.value.adjustments
        val exp = _editorState.value.exportConfig
        val id = "preset_${UUID.randomUUID().toString().replace("-", "").take(12)}"

        val domainPreset = PresetItem(
            id = id,
            name = cleanName,
            category = category,
            resolution = exp.resolution,
            fps = exp.fps,
            codec = exp.codec,
            bitrateMode = exp.bitrateMode,
            bitrateKbps = exp.effectiveBitrateKbps(),
            containerFormat = exp.containerFormat,
            crf = exp.crf,
            encoderPreset = exp.encoderPreset,
            brightness = adj.brightness,
            contrast = adj.contrast,
            saturation = adj.saturation,
            temperature = adj.temperature,
            sharpness = adj.sharpness,
            denoise = adj.denoise,
            isBuiltIn = false
        )

        viewModelScope.launch {
            localDao.upsertPreset(LocalPresetEntity.fromDomain(domainPreset))
            if (authenticatedUserId != null) {
                firestoreRepository.savePreset(
                    FirestoreVideoPreset(
                        id = id,
                        userId = authenticatedUserId,
                        name = cleanName,
                        category = category,
                        resolution = exp.resolution,
                        fps = exp.fps.toLong(),
                        codec = exp.codec,
                        bitrateMode = exp.bitrateMode,
                        bitrateKbps = exp.effectiveBitrateKbps().toLong(),
                        containerFormat = exp.containerFormat,
                        crf = exp.crf.toLong(),
                        encoderPreset = exp.encoderPreset,
                        brightness = adj.brightness.toLong(),
                        contrast = adj.contrast.toLong(),
                        saturation = adj.saturation.toLong(),
                        temperature = adj.temperature.toLong(),
                        sharpness = adj.sharpness.toLong(),
                        denoise = adj.denoise.toLong()
                    ),
                    customId = id
                )
            }
            _editorState.update { it.copy(statusMessage = "Saved preset '$cleanName'") }
        }
    }

    fun deletePreset(preset: PresetItem) {
        if (preset.isBuiltIn) return
        viewModelScope.launch {
            localDao.deletePreset(preset.id)
            if (authenticatedUserId != null) {
                firestoreRepository.deletePreset(preset.id)
            }
        }
    }

    fun requestStartProcessing(context: Context, operationType: String = "enhance") {
        val prefs = preferencesState.value
        val exp = _editorState.value.exportConfig
        val isHeavyJob = exp.resolution in listOf("2K", "4K") || exp.codec == "AV1"
        val wantsCloud = prefs.defaultRoute == ExecutionRoute.Cloud ||
            (prefs.defaultRoute == ExecutionRoute.Auto && isHeavyJob && authenticatedUserId != null)

        if (wantsCloud && authenticatedUserId != null && (prefs.askConsentEveryTime || !prefs.cloudConsentGranted)) {
            _editorState.update { it.copy(showCloudConsentDialog = true) }
            return
        }

        executeProcessingJob(
            context = context,
            operationType = operationType,
            useCloudRoute = wantsCloud && authenticatedUserId != null
        )
    }

    fun confirmCloudConsentAndStart(context: Context, rememberConsent: Boolean) {
        viewModelScope.launch {
            preferencesDataSource.updateCloudConsent(
                granted = true,
                askEveryTime = !rememberConsent
            )
            _editorState.update { it.copy(showCloudConsentDialog = false) }
            executeProcessingJob(
                context = context,
                operationType = "enhance",
                useCloudRoute = authenticatedUserId != null
            )
        }
    }

    fun dismissCloudConsentAndRunLocally(context: Context) {
        _editorState.update { it.copy(showCloudConsentDialog = false) }
        executeProcessingJob(
            context = context,
            operationType = "enhance",
            useCloudRoute = false
        )
    }

    fun executeProcessingJob(
        context: Context,
        operationType: String = "enhance",
        useCloudRoute: Boolean = false
    ) {
        if (_editorState.value.isProcessing) return

        activeProcessingJob = viewModelScope.launch {
            _editorState.update {
                it.copy(
                    isProcessing = true,
                    processingProgress = 2,
                    processingStatus = "Preparing source video...",
                    statusMessage = null
                )
            }

            // Ensure a real video file is loaded; if none selected yet, generate the real H.264 sample clip first
            var sourceMeta = _editorState.value.selectedVideo
            if (sourceMeta == null) {
                val sampleUri = VideoProcessingEngine.generateRealSampleVideoMp4(context, "Nature_Video.mp4").getOrNull()
                if (sampleUri != null) {
                    sourceMeta = VideoProcessingEngine.inspectVideo(context, sampleUri).getOrNull()
                    val thumb = VideoProcessingEngine.extractThumbnail(context, sampleUri)
                    _editorState.update { it.copy(selectedVideo = sourceMeta, videoThumbnail = thumb) }
                }
            }

            val validMeta = sourceMeta ?: run {
                _editorState.update {
                    it.copy(
                        isProcessing = false,
                        statusMessage = "Please select or import a video file first."
                    )
                }
                return@launch
            }

            val sourceUri = Uri.parse(validMeta.uriString)
            val adjustments = _editorState.value.adjustments
            val exportConfig = _editorState.value.exportConfig
            val jobId = "job_${UUID.randomUUID().toString().replace("-", "").take(12)}"
            val routeLabel = if (useCloudRoute && authenticatedUserId != null) "cloud" else "local"
            val summary = "${exportConfig.resolution} • ${exportConfig.fps} FPS • ${exportConfig.codec} • ${exportConfig.containerFormat}"

            val initialJob = JobItem(
                id = jobId,
                sourceFileName = validMeta.fileName,
                sourceUri = validMeta.uriString,
                sourceSizeBytes = validMeta.sizeBytes,
                operationType = operationType,
                executionRoute = routeLabel,
                configSummary = summary,
                status = "processing",
                progressPercent = 5
            )
            localDao.upsertJob(LocalJobEntity.fromDomain(initialJob))

            if (authenticatedUserId != null) {
                firestoreRepository.createJob(
                    FirestoreProcessingJob(
                        id = jobId,
                        userId = authenticatedUserId,
                        sourceFileName = validMeta.fileName,
                        sourceUri = validMeta.uriString,
                        sourceSizeBytes = validMeta.sizeBytes,
                        operationType = operationType,
                        executionRoute = routeLabel,
                        configSummary = summary,
                        status = "processing",
                        progressPercent = 5L,
                        outputSizeBytes = 0L
                    ),
                    customId = jobId
                )
            }

            val processResult = if (useCloudRoute && authenticatedUserId != null) {
                CloudWorkerClient.executeCloudOrHybridJob(
                    context = context,
                    endpointBaseUrl = preferencesState.value.cloudRunEndpointUrl,
                    jobId = jobId,
                    sourceUri = sourceUri,
                    sourceMetadata = validMeta,
                    adjustments = adjustments,
                    exportConfig = exportConfig,
                    onStatusAndProgress = { status, pct ->
                        _editorState.update {
                            it.copy(
                                processingStatus = status.replaceFirstChar { c -> c.uppercase() },
                                processingProgress = pct
                            )
                        }
                    }
                )
            } else {
                VideoProcessingEngine.processVideo(
                    context = context,
                    sourceUri = sourceUri,
                    sourceMetadata = validMeta,
                    adjustments = adjustments,
                    exportConfig = exportConfig,
                    onProgress = { pct ->
                        _editorState.update {
                            it.copy(
                                processingStatus = "Encoding ${exportConfig.codec} (${pct}%)",
                                processingProgress = pct
                            )
                        }
                    }
                )
            }

            processResult.onSuccess { output ->
                val completedJob = initialJob.copy(
                    status = "completed",
                    progressPercent = 100,
                    outputFilePath = output.outputUri.toString(),
                    outputSizeBytes = output.outputSizeBytes,
                    updatedAtMillis = System.currentTimeMillis()
                )
                localDao.upsertJob(LocalJobEntity.fromDomain(completedJob))

                val exportId = "exp_${UUID.randomUUID().toString().replace("-", "").take(12)}"
                val exportItem = ExportItem(
                    id = exportId,
                    jobId = jobId,
                    fileName = output.fileName,
                    originalSizeBytes = output.originalSizeBytes,
                    outputSizeBytes = output.outputSizeBytes,
                    savedPercent = output.savedPercent,
                    resolution = output.resolutionLabel,
                    fps = output.fps,
                    codec = output.codec,
                    containerFormat = output.containerFormat,
                    durationMs = output.durationMs,
                    localUri = output.outputUri.toString()
                )
                localDao.upsertExport(LocalExportEntity.fromDomain(exportItem))

                if (authenticatedUserId != null) {
                    firestoreRepository.updateJobProgress(
                        jobId = jobId,
                        status = "completed",
                        progressPercent = 100,
                        outputFilePath = output.outputUri.toString(),
                        outputSizeBytes = output.outputSizeBytes
                    )
                    firestoreRepository.saveExportRecord(
                        FirestoreExportRecord(
                            id = exportId,
                            userId = authenticatedUserId,
                            jobId = jobId,
                            fileName = output.fileName,
                            originalSizeBytes = output.originalSizeBytes,
                            outputSizeBytes = output.outputSizeBytes,
                            savedPercent = output.savedPercent.toLong(),
                            resolution = output.resolutionLabel,
                            fps = output.fps.toLong(),
                            codec = output.codec,
                            containerFormat = output.containerFormat,
                            durationMs = output.durationMs,
                            localUri = output.outputUri.toString()
                        ),
                        customId = exportId
                    )
                }

                val outThumb = VideoProcessingEngine.extractThumbnail(context, output.outputUri)
                _editorState.update { state ->
                    state.copy(
                        isProcessing = false,
                        processingProgress = 100,
                        processingStatus = "Completed",
                        lastCompletedOutput = output,
                        videoThumbnail = outThumb ?: state.videoThumbnail,
                        activeScreen = PrimaryScreen.ProcessingComplete
                    )
                }
            }.onFailure { err ->
                val failedJob = initialJob.copy(
                    status = "failed",
                    errorCode = "ENCODE_ERROR",
                    errorMessage = err.localizedMessage ?: "Encoding failed",
                    updatedAtMillis = System.currentTimeMillis()
                )
                localDao.upsertJob(LocalJobEntity.fromDomain(failedJob))
                if (authenticatedUserId != null) {
                    firestoreRepository.updateJobProgress(
                        jobId = jobId,
                        status = "failed",
                        progressPercent = _editorState.value.processingProgress,
                        errorCode = "ENCODE_ERROR",
                        errorMessage = err.localizedMessage ?: "Encoding failed"
                    )
                }
                _editorState.update {
                    it.copy(
                        isProcessing = false,
                        processingStatus = "Failed",
                        statusMessage = "Processing failed: ${err.localizedMessage ?: "Encoder error"}"
                    )
                }
            }
        }
    }

    fun cancelActiveProcessing() {
        activeProcessingJob?.cancel()
        activeProcessingJob = null
        _editorState.update {
            it.copy(
                isProcessing = false,
                processingStatus = "Cancelled",
                statusMessage = "Processing job cancelled."
            )
        }
    }

    fun deleteExportItem(exportItem: ExportItem) {
        viewModelScope.launch {
            localDao.deleteExport(exportItem.id)
            if (authenticatedUserId != null) {
                firestoreRepository.deleteExportRecord(exportItem.id)
            }
        }
    }

    // Theme & Preferences Updates
    fun setThemePalette(palette: ThemePalette) {
        viewModelScope.launch {
            preferencesDataSource.updateThemePalette(palette)
            syncPreferencesToFirestore()
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferencesDataSource.updateThemeMode(mode)
            syncPreferencesToFirestore()
        }
    }

    fun setDefaultRoute(route: ExecutionRoute) {
        viewModelScope.launch {
            preferencesDataSource.updateDefaultRoute(route)
            syncPreferencesToFirestore()
        }
    }

    fun setPreserveMetadata(preserve: Boolean) {
        viewModelScope.launch {
            preferencesDataSource.updatePreserveMetadata(preserve)
            _editorState.update {
                it.copy(exportConfig = it.exportConfig.copy(preserveMetadata = preserve))
            }
            syncPreferencesToFirestore()
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesDataSource.updateNotificationsEnabled(enabled)
            syncPreferencesToFirestore()
        }
    }

    private suspend fun syncPreferencesToFirestore() {
        if (authenticatedUserId == null) return
        val prefs = preferencesState.value
        firestoreRepository.savePreferences(
            FirestoreUserPreferences(
                userId = authenticatedUserId,
                themePalette = prefs.themePalette.name,
                themeMode = prefs.themeMode.name,
                defaultRouting = prefs.defaultRoute.name,
                cloudConsentAskEveryTime = prefs.askConsentEveryTime,
                preserveMetadata = prefs.preserveMetadata,
                notificationsEnabled = prefs.notificationsEnabled
            )
        )
    }

    companion object {
        fun formatBytes(bytes: Long): String {
            if (bytes <= 0L) return "0 MB"
            val mb = bytes.toDouble() / (1024.0 * 1024.0)
            return if (mb >= 1.0) {
                "%.1f MB".format(mb)
            } else {
                val kb = bytes.toDouble() / 1024.0
                "%.0f KB".format(kb)
            }
        }

        fun provideFactory(
            context: Context,
            authenticatedUserId: String?
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val appContext = context.applicationContext
                val prefs = PreferencesDataSource(appContext)
                val db = EliteMatrixDatabase.getInstance(appContext)
                val databaseId = appContext.getString(R.string.firestore_database_id)
                val firestore = FirebaseFirestore.getInstance(databaseId)
                val firestoreRepo = EliteMatrixFirestoreRepository(firestore, Firebase.auth)
                EliteMatrixViewModel(
                    preferencesDataSource = prefs,
                    localDao = db.dao(),
                    firestoreRepository = firestoreRepo,
                    authenticatedUserId = authenticatedUserId
                )
            }
        }
    }
}
