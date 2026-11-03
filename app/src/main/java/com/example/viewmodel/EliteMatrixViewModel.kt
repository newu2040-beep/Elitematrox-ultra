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
import com.example.domain.model.AppFontFamily
import com.example.domain.model.AspectRatioMode
import com.example.domain.model.BuiltInPresets
import com.example.domain.model.CinematicColorProfile
import com.example.domain.model.DeviceHardwareCapabilities
import com.example.domain.model.EnhancementAdjustments
import com.example.domain.model.ExecutionRoute
import com.example.domain.model.ExportConfiguration
import com.example.domain.model.ExportItem
import com.example.domain.model.JobItem
import com.example.domain.model.PresetItem
import com.example.domain.model.ProcessingPerformanceMode
import com.example.domain.model.SocialMediaPreset
import com.example.domain.model.ThemeMode
import com.example.domain.model.ThemePalette
import com.example.domain.model.VideoMetadata
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CancellationException
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
    val deviceCapabilities: DeviceHardwareCapabilities = DeviceHardwareCapabilities(),
    val isProcessing: Boolean = false,
    val isPaused: Boolean = false,
    val activeJobId: String? = null,
    val processingStatus: String = "Idle",
    val processingProgress: Int = 0,
    val elapsedTimeMs: Long = 0L,
    val estimatedRemainingMs: Long = 0L,
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
    private val authenticatedUserId: String?,
    initialDeviceCapabilities: DeviceHardwareCapabilities = DeviceHardwareCapabilities()
) : ViewModel() {

    private val _editorState = MutableStateFlow(
        EditorUiState(
            deviceCapabilities = initialDeviceCapabilities,
            exportConfig = ExportConfiguration(
                performanceMode = initialDeviceCapabilities.recommendedPerformanceMode
            )
        )
    )
    val editorState: StateFlow<EditorUiState> = _editorState.asStateFlow()

    private var activeProcessingJob: Job? = null
    private val pauseFlag = AtomicBoolean(false)

    val preferencesState: StateFlow<AppPreferencesState> = preferencesDataSource.preferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = AppPreferencesState()
        )

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

    fun refreshDeviceCapabilities(context: Context) {
        val caps = VideoProcessingEngine.detectDeviceCapabilities(context)
        _editorState.update { it.copy(deviceCapabilities = caps) }
    }

    fun applyRecommendedHardwareSettings() {
        val caps = _editorState.value.deviceCapabilities
        val targetFps = when {
            caps.displayRefreshRateHz >= 120 -> 120
            caps.displayRefreshRateHz >= 90 -> 90
            else -> 60
        }
        _editorState.update { state ->
            state.copy(
                exportConfig = state.exportConfig.copy(
                    resolution = caps.recommendedResolution,
                    fps = targetFps,
                    codec = if ("H.265" in caps.supportedCodecs && targetFps >= 90) "H.265" else "H.264",
                    bitrateMode = "Auto",
                    performanceMode = caps.recommendedPerformanceMode,
                    encoderPreset = caps.recommendedPerformanceMode.ffmpegPreset
                ),
                statusMessage = "Auto-tuned for ${caps.displayRefreshRateHz}Hz display: ${caps.recommendedPerformanceMode.badge} • ${caps.recommendedResolution} • $targetFps FPS"
            )
        }
    }

    fun setPerformanceMode(mode: ProcessingPerformanceMode) {
        _editorState.update { state ->
            state.copy(
                exportConfig = state.exportConfig.copy(
                    performanceMode = mode,
                    encoderPreset = mode.ffmpegPreset
                ),
                statusMessage = "Activated ${mode.title} (${mode.subtitle})"
            )
        }
    }

    fun applyCinematicProfile(profile: CinematicColorProfile) {
        _editorState.update { state ->
            val current = state.adjustments
            val updated = when (profile) {
                CinematicColorProfile.Neutral -> current.copy(cinematicProfile = profile)
                CinematicColorProfile.TealAndOrange -> current.copy(
                    cinematicProfile = profile,
                    contrast = 18,
                    saturation = 12,
                    temperature = 14,
                    vibrance = 16,
                    highlights = -10,
                    shadows = 12
                )
                CinematicColorProfile.HdrVivid -> current.copy(
                    cinematicProfile = profile,
                    brightness = 8,
                    contrast = 22,
                    saturation = 24,
                    vibrance = 28,
                    sharpness = 35,
                    highlights = 14
                )
                CinematicColorProfile.GoldenHour -> current.copy(
                    cinematicProfile = profile,
                    brightness = 10,
                    contrast = 12,
                    saturation = 16,
                    temperature = 32,
                    tint = 8,
                    exposure = 8
                )
                CinematicColorProfile.CrispDaylight -> current.copy(
                    cinematicProfile = profile,
                    brightness = 12,
                    contrast = 15,
                    saturation = 10,
                    temperature = -14,
                    sharpness = 28,
                    exposure = 6
                )
                CinematicColorProfile.MoodyMatte -> current.copy(
                    cinematicProfile = profile,
                    brightness = -6,
                    contrast = 20,
                    saturation = -22,
                    shadows = 18,
                    highlights = -18,
                    temperature = -8
                )
                CinematicColorProfile.CyberpunkNeon -> current.copy(
                    cinematicProfile = profile,
                    contrast = 24,
                    saturation = 30,
                    vibrance = 34,
                    tint = 22,
                    temperature = -16,
                    highlights = 16
                )
                CinematicColorProfile.MonochromeBw -> current.copy(
                    cinematicProfile = profile,
                    contrast = 26,
                    saturation = -100,
                    vibrance = -100,
                    sharpness = 24
                )
            }
            state.copy(
                adjustments = updated,
                statusMessage = "Applied ${profile.label} color profile"
            )
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
                val thumb = VideoProcessingEngine.extractThumbnail(
                    context = context,
                    uri = uri,
                    manualRotationDegrees = _editorState.value.adjustments.rotationDegrees
                )
                _editorState.update { state ->
                    // Reset manual rotation when importing a fresh video so container rotation is respected cleanly
                    val cleanAdj = state.adjustments.copy(
                        rotationDegrees = 0,
                        aspectRatioMode = AspectRatioMode.Original,
                        cropRatio = "Original"
                    )
                    state.copy(
                        selectedVideo = meta,
                        videoThumbnail = thumb,
                        adjustments = cleanAdj,
                        activeScreen = targetScreenAfterImport ?: state.activeScreen,
                        statusMessage = "Loaded ${meta.fileName} (${meta.exactDimensionsLabel} • ${meta.orientationCategoryLabel} • ${meta.formattedFpsLabel})"
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
                        statusMessage = "Loaded Nature_Video.mp4 (${meta?.exactDimensionsLabel ?: "640×360"} • ${meta?.orientationCategoryLabel ?: "Landscape (16:9)"})"
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
                    containerFormat = preset.containerFormat,
                    audioBitrateKbps = preset.audioBitrateKbps,
                    socialOptimizationEnabled = true
                ),
                statusMessage = "Applied ${preset.title} Anti-Recompression Profile (${preset.subtitle})"
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
        if (_editorState.value.isProcessing) {
            _editorState.update {
                it.copy(statusMessage = "A video processing job is already running. Pause or cancel it first.")
            }
            return
        }

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
        useCloudRoute: Boolean = false,
        existingJobId: String? = null
    ) {
        if (_editorState.value.isProcessing) return
        pauseFlag.set(false)

        activeProcessingJob = viewModelScope.launch {
            val jobId = existingJobId ?: "job_${UUID.randomUUID().toString().replace("-", "").take(12)}"
            _editorState.update {
                it.copy(
                    isProcessing = true,
                    isPaused = false,
                    activeJobId = jobId,
                    processingProgress = 2,
                    elapsedTimeMs = 0L,
                    estimatedRemainingMs = 0L,
                    processingStatus = "Preparing source video...",
                    statusMessage = null
                )
            }

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
                        isPaused = false,
                        activeJobId = null,
                        statusMessage = "Please select or import a video file first."
                    )
                }
                return@launch
            }

            val sourceUri = Uri.parse(validMeta.uriString)
            val adjustments = _editorState.value.adjustments
            val exportConfig = _editorState.value.exportConfig
            val isCompressionWorkflow = operationType.equals("compress", ignoreCase = true)
            val targetBitrateKbps = exportConfig.effectiveBitrateKbps(isCompressionWorkflow)
            val (targetW, targetH) = exportConfig.targetDimensionsPx(
                sourceWidth = validMeta.displayWidth,
                sourceHeight = validMeta.displayHeight,
                manualRotationDegrees = adjustments.rotationDegrees,
                aspectRatioMode = adjustments.aspectRatioMode
            )
            val routeLabel = if (useCloudRoute && authenticatedUserId != null) "cloud" else "local"
            val summary = "${exportConfig.performanceMode.badge} • ${targetW}×${targetH} (${exportConfig.resolution}) • ${exportConfig.fps} FPS • ${"%.1f".format(targetBitrateKbps / 1000f)} Mbps • ${exportConfig.codec} • ${exportConfig.containerFormat}"

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
                    isCompressionWorkflow = isCompressionWorkflow,
                    isPausedProvider = { pauseFlag.get() },
                    onDetailedProgress = { pct, elapsed, eta, phase ->
                        _editorState.update { state ->
                            state.copy(
                                processingStatus = if (pauseFlag.get()) "Paused • $phase" else phase,
                                processingProgress = pct,
                                elapsedTimeMs = elapsed,
                                estimatedRemainingMs = eta
                            )
                        }
                    },
                    onProgress = { pct ->
                        viewModelScope.launch {
                            localDao.upsertJob(
                                LocalJobEntity.fromDomain(
                                    initialJob.copy(
                                        status = if (pauseFlag.get()) "paused" else "processing",
                                        progressPercent = pct,
                                        updatedAtMillis = System.currentTimeMillis()
                                    )
                                )
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
                    resolution = "${output.width}×${output.height} (${output.resolutionLabel})",
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
                        isPaused = false,
                        activeJobId = null,
                        processingProgress = 100,
                        processingStatus = "Completed",
                        lastCompletedOutput = output,
                        videoThumbnail = outThumb ?: state.videoThumbnail,
                        activeScreen = PrimaryScreen.ProcessingComplete
                    )
                }
            }.onFailure { err ->
                if (err is CancellationException) return@onFailure
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
                        isPaused = false,
                        activeJobId = null,
                        processingStatus = "Failed",
                        statusMessage = "Processing failed: ${err.localizedMessage ?: "Encoder error"}"
                    )
                }
            }
        }
    }

    fun pauseActiveProcessing() {
        if (!_editorState.value.isProcessing) return
        pauseFlag.set(true)
        val jobId = _editorState.value.activeJobId
        _editorState.update {
            it.copy(
                isPaused = true,
                processingStatus = "Paused (${it.processingProgress}%)",
                statusMessage = "Processing paused. Tap Resume to continue."
            )
        }
        if (jobId != null) {
            viewModelScope.launch {
                val existing = jobsState.value.firstOrNull { it.id == jobId }
                if (existing != null) {
                    localDao.upsertJob(
                        LocalJobEntity.fromDomain(
                            existing.copy(
                                status = "paused",
                                updatedAtMillis = System.currentTimeMillis()
                            )
                        )
                    )
                }
            }
        }
    }

    fun resumeActiveProcessing() {
        if (!_editorState.value.isProcessing) return
        pauseFlag.set(false)
        val jobId = _editorState.value.activeJobId
        _editorState.update {
            it.copy(
                isPaused = false,
                processingStatus = "Resuming pipeline (${it.processingProgress}%)...",
                statusMessage = "Resumed video processing."
            )
        }
        if (jobId != null) {
            viewModelScope.launch {
                val existing = jobsState.value.firstOrNull { it.id == jobId }
                if (existing != null) {
                    localDao.upsertJob(
                        LocalJobEntity.fromDomain(
                            existing.copy(
                                status = "processing",
                                updatedAtMillis = System.currentTimeMillis()
                            )
                        )
                    )
                }
            }
        }
    }

    fun cancelActiveProcessing() {
        val jobId = _editorState.value.activeJobId
        pauseFlag.set(false)
        activeProcessingJob?.cancel()
        activeProcessingJob = null
        _editorState.update {
            it.copy(
                isProcessing = false,
                isPaused = false,
                activeJobId = null,
                processingStatus = "Cancelled",
                statusMessage = "Processing job cancelled."
            )
        }
        if (jobId != null) {
            viewModelScope.launch {
                val existing = jobsState.value.firstOrNull { it.id == jobId }
                if (existing != null) {
                    localDao.upsertJob(
                        LocalJobEntity.fromDomain(
                            existing.copy(
                                status = "cancelled",
                                updatedAtMillis = System.currentTimeMillis()
                            )
                        )
                    )
                }
                if (authenticatedUserId != null) {
                    firestoreRepository.updateJobProgress(
                        jobId = jobId,
                        status = "cancelled",
                        progressPercent = _editorState.value.processingProgress
                    )
                }
            }
        }
    }

    fun retryJob(context: Context, job: JobItem) {
        if (_editorState.value.isProcessing) {
            _editorState.update {
                it.copy(statusMessage = "Wait for the active job to finish or cancel it before retrying.")
            }
            return
        }
        viewModelScope.launch {
            if (job.sourceUri.isNotBlank()) {
                val inspected = VideoProcessingEngine.inspectVideo(context, Uri.parse(job.sourceUri)).getOrNull()
                if (inspected != null) {
                    val thumb = VideoProcessingEngine.extractThumbnail(context, Uri.parse(job.sourceUri))
                    _editorState.update { it.copy(selectedVideo = inspected, videoThumbnail = thumb) }
                }
            }
            executeProcessingJob(
                context = context,
                operationType = job.operationType,
                useCloudRoute = job.executionRoute.equals("cloud", ignoreCase = true),
                existingJobId = job.id
            )
        }
    }

    fun removeJob(job: JobItem) {
        if (_editorState.value.activeJobId == job.id && _editorState.value.isProcessing) {
            cancelActiveProcessing()
        }
        viewModelScope.launch {
            localDao.deleteJob(job.id)
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

    // Theme, Font & Preferences Updates
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

    fun setFontFamily(fontFamily: AppFontFamily) {
        viewModelScope.launch {
            preferencesDataSource.updateFontFamily(fontFamily)
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

        fun formatDurationCompact(ms: Long): String {
            if (ms <= 0L) return "0s"
            val totalSec = (ms / 1000L).coerceAtLeast(1L)
            val m = totalSec / 60L
            val s = totalSec % 60L
            return if (m > 0L) "${m}m ${s}s" else "${s}s"
        }

        fun formatDurationSeconds(ms: Long): String = formatDurationCompact(ms)

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
                val detectedCaps = VideoProcessingEngine.detectDeviceCapabilities(appContext)
                EliteMatrixViewModel(
                    preferencesDataSource = prefs,
                    localDao = db.dao(),
                    firestoreRepository = firestoreRepo,
                    authenticatedUserId = authenticatedUserId,
                    initialDeviceCapabilities = detectedCaps
                )
            }
        }
    }
}
