package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppPreferencesState
import com.example.data.local.PreferencesDataSource
import com.example.feature.auth.WelcomeLoginScreen
import com.example.feature.auth.signOut
import com.example.feature.editor.CloudConsentDialog
import com.example.feature.editor.EnhanceVideoScreen
import com.example.feature.editor.ExportSettingsScreen
import com.example.feature.editor.ProcessingCompleteScreen
import com.example.feature.editor.VideoPlaybackModalDialog
import com.example.feature.editor.shareVideoFile
import com.example.feature.home.EliteMatrixBottomNavigationBar
import com.example.feature.home.HomeDashboardScreen
import com.example.feature.tools.ProjectsHistoryScreen
import com.example.feature.tools.SettingsScreen
import com.example.feature.tools.ToolsHubScreen
import com.example.ui.theme.EliteMatrixTheme
import com.example.ui.theme.LocalNeumorphicColors
import com.example.ui.theme.neumorphicRaised
import com.example.viewmodel.ActiveToolModal
import com.example.viewmodel.EliteMatrixViewModel
import com.example.viewmodel.PrimaryScreen
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import java.io.File
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EliteMatrixRootApp()
        }
    }
}

@Composable
fun EliteMatrixRootApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val preferencesDataSource = remember(context) { PreferencesDataSource(context.applicationContext) }
    val appPrefs by preferencesDataSource.preferencesFlow.collectAsStateWithLifecycle(
        initialValue = AppPreferencesState()
    )

    var currentUser by remember {
        mutableStateOf<FirebaseUser?>(
            try {
                Firebase.auth.currentUser
            } catch (_: Exception) {
                null
            }
        )
    }

    DisposableEffect(Unit) {
        val auth = try {
            Firebase.auth
        } catch (_: Exception) {
            null
        }
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            currentUser = firebaseAuth.currentUser
        }
        auth?.addAuthStateListener(listener)
        onDispose {
            auth?.removeAuthStateListener(listener)
        }
    }

    EliteMatrixTheme(
        themePalette = appPrefs.themePalette,
        themeMode = appPrefs.themeMode,
        fontFamily = appPrefs.fontFamily
    ) {
        val isAuthenticated = currentUser != null
        val isGuest = appPrefs.isGuestMode

        if (!isAuthenticated && !isGuest) {
            // Screen 1 — Welcome / Login (Auth Gate)
            WelcomeLoginScreen(
                onGoogleAuthSuccess = {
                    currentUser = Firebase.auth.currentUser
                    scope.launch {
                        preferencesDataSource.setGuestMode(false)
                    }
                },
                onEnterGuestMode = {
                    scope.launch {
                        preferencesDataSource.setGuestMode(true)
                    }
                }
            )
        } else {
            val viewModelKey = currentUser?.uid ?: "guest_local_session"
            val vm: EliteMatrixViewModel = viewModel(
                key = viewModelKey,
                factory = EliteMatrixViewModel.provideFactory(
                    context = context,
                    authenticatedUserId = currentUser?.uid
                )
            )

            EliteMatrixAuthenticatedOrGuestContent(
                viewModel = vm,
                currentUser = currentUser,
                isGuestMode = !isAuthenticated && isGuest,
                onSignOutOrExitGuest = {
                    val credentialManager = CredentialManager.create(context)
                    signOut(
                        context = context,
                        credentialManager = credentialManager,
                        onSignOutComplete = {
                            currentUser = null
                            scope.launch {
                                preferencesDataSource.setGuestMode(false)
                            }
                        },
                        scope = scope
                    )
                }
            )
        }
    }
}

@Composable
private fun EliteMatrixAuthenticatedOrGuestContent(
    viewModel: EliteMatrixViewModel,
    currentUser: FirebaseUser?,
    isGuestMode: Boolean,
    onSignOutOrExitGuest: () -> Unit
) {
    val context = LocalContext.current
    val neu = LocalNeumorphicColors.current

    val editorState by viewModel.editorState.collectAsStateWithLifecycle()
    val prefsState by viewModel.preferencesState.collectAsStateWithLifecycle()
    val presets by viewModel.presetsState.collectAsStateWithLifecycle()
    val jobs by viewModel.jobsState.collectAsStateWithLifecycle()
    val exports by viewModel.exportsState.collectAsStateWithLifecycle()

    var pendingDestinationAfterImport by remember { mutableStateOf<PrimaryScreen?>(null) }

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    var hasGalleryStorageAccess by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grantResults ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            grantResults[Manifest.permission.POST_NOTIFICATIONS]?.let {
                hasNotificationPermission = it
            }
        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            grantResults[Manifest.permission.WRITE_EXTERNAL_STORAGE]?.let {
                hasGalleryStorageAccess = it
            }
        }
    }

    fun requestAllRuntimePermissions() {
        val permsToRequest = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            permsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (permsToRequest.isNotEmpty()) {
            permissionsLauncher.launch(permsToRequest.toTypedArray())
        }
    }

    // Request permissions on startup, detect hardware display capabilities, and load real H.264 sample video if none is loaded yet
    LaunchedEffect(Unit) {
        viewModel.refreshDeviceCapabilities(context)
        requestAllRuntimePermissions()
        if (editorState.selectedVideo == null) {
            viewModel.loadOrGenerateSampleVideo(context, targetScreen = null)
        }
    }

    // Android Photo Picker for Gallery videos
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
            }
            viewModel.importVideoUri(context, uri, pendingDestinationAfterImport)
        }
        pendingDestinationAfterImport = null
    }

    // Full Storage / Files Document Picker for browsing any video file across device folders/drives
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
            }
            viewModel.importVideoUri(
                context,
                uri,
                pendingDestinationAfterImport ?: PrimaryScreen.EnhanceVideo
            )
        }
        pendingDestinationAfterImport = null
    }

    val showBottomBar = editorState.activeScreen in listOf(
        PrimaryScreen.Home,
        PrimaryScreen.ToolsHub,
        PrimaryScreen.ProjectsHistory,
        PrimaryScreen.SettingsScreen
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = neu.canvas,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            if (showBottomBar) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.widthIn(max = 600.dp)) {
                        EliteMatrixBottomNavigationBar(
                            activeScreen = editorState.activeScreen,
                            onSelectTab = { tab ->
                                viewModel.openToolModal(ActiveToolModal.None)
                                viewModel.navigateTo(tab)
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(neu.canvas)
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            val contentMaxWidth = if (maxWidth >= 600.dp) 620.dp else 560.dp
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = contentMaxWidth)
            ) {
                // Non-intrusive Neumorphic status toast banner
                AnimatedVisibility(
                    visible = !editorState.statusMessage.isNullOrBlank(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    editorState.statusMessage?.let { msg ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 6.dp)
                                .neumorphicRaised(cornerRadius = 16.dp, elevation = 3.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { viewModel.clearStatusMessage() }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = msg,
                                    color = neu.textPrimary,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.5.sp
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { viewModel.clearStatusMessage() }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Dismiss notification",
                                        tint = neu.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    when (editorState.activeScreen) {
                        PrimaryScreen.Home -> {
                            HomeDashboardScreen(
                                userName = currentUser?.displayName,
                                userPhotoUrl = currentUser?.photoUrl?.toString(),
                                isGuestMode = isGuestMode,
                                selectedVideo = editorState.selectedVideo,
                                videoThumbnail = editorState.videoThumbnail,
                                recentExports = exports,
                                activeJobs = jobs,
                                isProcessing = editorState.isProcessing,
                                isPaused = editorState.isPaused,
                                jobStage = editorState.jobStage,
                                processingProgress = editorState.processingProgress,
                                processingStatus = editorState.processingStatus,
                                elapsedTimeMs = editorState.elapsedTimeMs,
                                estimatedRemainingMs = editorState.estimatedRemainingMs,
                                processingSpeedLabel = editorState.processingSpeedLabel,
                                performanceMode = editorState.exportConfig.performanceMode,
                                freeStorageBytes = editorState.deviceCapabilities.freeStorageBytes,
                                onSelectPerformanceMode = { mode -> viewModel.setPerformanceMode(mode) },
                                onPauseProcessing = { viewModel.pauseActiveProcessing() },
                                onResumeProcessing = { viewModel.resumeActiveProcessing() },
                                onCancelProcessing = { viewModel.cancelActiveProcessing() },
                                onImportVideoClick = {
                                    pendingDestinationAfterImport = null
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                    )
                                },
                                onLoadSampleVideoClick = {
                                    viewModel.loadOrGenerateSampleVideo(context, targetScreen = null)
                                },
                                onNavigatePrimary = { screen ->
                                    viewModel.navigateTo(screen)
                                },
                                onOpenToolModal = { modal ->
                                    viewModel.openToolModal(modal)
                                    viewModel.navigateTo(PrimaryScreen.ToolsHub)
                                },
                                onOpenExportedVideo = { exportItem ->
                                    if (exportItem.localUri.isNotBlank()) {
                                        viewModel.openVideoPlayer(exportItem.localUri)
                                    } else if (editorState.selectedVideo != null) {
                                        viewModel.openVideoPlayer(editorState.selectedVideo!!.uriString)
                                    }
                                },
                                onShareExportedVideo = { exportItem ->
                                    val path = android.net.Uri.parse(exportItem.localUri).path
                                    if (!path.isNullOrBlank()) {
                                        shareVideoFile(context, File(path))
                                    }
                                },
                                onDeleteExportedVideo = { exportItem ->
                                    viewModel.deleteExportItem(exportItem)
                                },
                                onSignOutOrLinkAccount = onSignOutOrExitGuest
                            )
                        }

                        PrimaryScreen.EnhanceVideo -> {
                            EnhanceVideoScreen(
                                selectedVideo = editorState.selectedVideo,
                                thumbnailBitmap = editorState.videoThumbnail,
                                adjustments = editorState.adjustments,
                                exportConfig = editorState.exportConfig,
                                favoriteFilterIds = editorState.favoriteFilterIds,
                                onUpdateAdjustments = { transform ->
                                    viewModel.updateAdjustments(transform)
                                },
                                onApplyCinematicProfile = { profile ->
                                    viewModel.applyCinematicProfile(profile)
                                },
                                onApplyCreativeFilter = { filter ->
                                    viewModel.applyCreativeFilter(filter)
                                },
                                onToggleFavoriteFilter = { filterId ->
                                    viewModel.toggleFavoriteFilter(filterId)
                                },
                                onResetAdjustments = {
                                    viewModel.resetAdjustments()
                                },
                                onSavePreset = { presetName ->
                                    viewModel.saveCurrentAsPreset(presetName, category = "enhance")
                                },
                                onImportNewVideo = {
                                    pendingDestinationAfterImport = PrimaryScreen.EnhanceVideo
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                    )
                                },
                                onLoadSampleVideo = {
                                    viewModel.loadOrGenerateSampleVideo(
                                        context,
                                        targetScreen = PrimaryScreen.EnhanceVideo
                                    )
                                },
                                onOpenFullVideoPlayer = { uriStr ->
                                    viewModel.openVideoPlayer(uriStr)
                                },
                                onApplyChangesNavigateToExport = {
                                    viewModel.navigateTo(PrimaryScreen.ExportSettings)
                                },
                                onBack = {
                                    viewModel.navigateTo(PrimaryScreen.Home)
                                }
                            )
                        }

                        PrimaryScreen.ExportSettings -> {
                            ExportSettingsScreen(
                                selectedVideo = editorState.selectedVideo,
                                adjustments = editorState.adjustments,
                                exportConfig = editorState.exportConfig,
                                deviceCapabilities = editorState.deviceCapabilities,
                                isProcessing = editorState.isProcessing,
                                isPaused = editorState.isPaused,
                                processingProgress = editorState.processingProgress,
                                processingStatus = editorState.processingStatus,
                                elapsedTimeMs = editorState.elapsedTimeMs,
                                estimatedRemainingMs = editorState.estimatedRemainingMs,
                                onUpdateExportConfig = { transform ->
                                    viewModel.updateExportConfig(transform)
                                },
                                onSelectPerformanceMode = { mode ->
                                    viewModel.setPerformanceMode(mode)
                                },
                                onApplyRecommendedHardwareSettings = {
                                    viewModel.applyRecommendedHardwareSettings()
                                },
                                onSelectSocialPreset = { socialPreset ->
                                    viewModel.applySocialPreset(socialPreset)
                                },
                                onSavePreset = { presetName ->
                                    viewModel.saveCurrentAsPreset(presetName, category = "export")
                                },
                                onStartExportJob = {
                                    viewModel.requestStartProcessing(context, operationType = "export")
                                },
                                onPauseProcessing = {
                                    viewModel.pauseActiveProcessing()
                                },
                                onResumeProcessing = {
                                    viewModel.resumeActiveProcessing()
                                },
                                onCancelProcessing = {
                                    viewModel.cancelActiveProcessing()
                                },
                                onBack = {
                                    viewModel.navigateTo(PrimaryScreen.EnhanceVideo)
                                }
                            )
                        }

                        PrimaryScreen.ProcessingComplete -> {
                            ProcessingCompleteScreen(
                                completedOutput = editorState.lastCompletedOutput,
                                thumbnailBitmap = editorState.videoThumbnail,
                                onOpenVideo = { uriStr ->
                                    viewModel.openVideoPlayer(uriStr)
                                },
                                onGoToProjects = {
                                    viewModel.navigateTo(PrimaryScreen.ProjectsHistory)
                                },
                                onBackToHome = {
                                    viewModel.navigateTo(PrimaryScreen.Home)
                                }
                            )
                        }

                        PrimaryScreen.ToolsHub -> {
                            ToolsHubScreen(
                                selectedVideo = editorState.selectedVideo,
                                exportConfig = editorState.exportConfig,
                                presets = presets,
                                activeModal = editorState.activeToolModal,
                                onOpenModal = { modal ->
                                    viewModel.openToolModal(modal)
                                },
                                onNavigateScreen = { screen ->
                                    viewModel.navigateTo(screen)
                                },
                                onUpdateExportConfig = { transform ->
                                    viewModel.updateExportConfig(transform)
                                },
                                onApplyPreset = { preset ->
                                    viewModel.applyPresetItem(preset)
                                },
                                onSaveNewPreset = { name ->
                                    viewModel.saveCurrentAsPreset(name, category = "custom")
                                },
                                onDeletePreset = { preset ->
                                    viewModel.deletePreset(preset)
                                },
                                onStartJob = { opType ->
                                    viewModel.requestStartProcessing(context, operationType = opType)
                                },
                                onImportVideo = {
                                    pendingDestinationAfterImport = PrimaryScreen.ToolsHub
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                    )
                                },
                                onLoadSampleVideo = {
                                    viewModel.loadOrGenerateSampleVideo(context, targetScreen = PrimaryScreen.ToolsHub)
                                },
                                onBackToHome = {
                                    viewModel.openToolModal(ActiveToolModal.None)
                                    viewModel.navigateTo(PrimaryScreen.Home)
                                }
                            )
                        }

                        PrimaryScreen.ProjectsHistory -> {
                            ProjectsHistoryScreen(
                                jobs = jobs,
                                exports = exports,
                                thumbnailBitmap = editorState.videoThumbnail,
                                isProcessing = editorState.isProcessing,
                                isPaused = editorState.isPaused,
                                activeJobId = editorState.activeJobId,
                                elapsedTimeMs = editorState.elapsedTimeMs,
                                estimatedRemainingMs = editorState.estimatedRemainingMs,
                                onPauseActiveJob = { viewModel.pauseActiveProcessing() },
                                onResumeActiveJob = { viewModel.resumeActiveProcessing() },
                                onCancelActiveJob = { viewModel.cancelActiveProcessing() },
                                onRetryJob = { job -> viewModel.retryJob(context, job) },
                                onRemoveJob = { job -> viewModel.removeJob(job) },
                                onOpenExport = { exportItem ->
                                    if (exportItem.localUri.isNotBlank()) {
                                        viewModel.openVideoPlayer(exportItem.localUri)
                                    } else if (editorState.selectedVideo != null) {
                                        viewModel.openVideoPlayer(editorState.selectedVideo!!.uriString)
                                    }
                                },
                                onShareExport = { exportItem ->
                                    val path = android.net.Uri.parse(exportItem.localUri).path
                                    if (!path.isNullOrBlank()) {
                                        shareVideoFile(context, File(path))
                                    }
                                },
                                onDeleteExport = { exportItem ->
                                    viewModel.deleteExportItem(exportItem)
                                },
                                onImportNewVideo = {
                                    pendingDestinationAfterImport = PrimaryScreen.EnhanceVideo
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                    )
                                },
                                onBackToHome = {
                                    viewModel.navigateTo(PrimaryScreen.Home)
                                }
                            )
                        }

                        PrimaryScreen.SettingsScreen -> {
                            SettingsScreen(
                                userEmail = currentUser?.email,
                                userName = currentUser?.displayName,
                                isGuestMode = isGuestMode,
                                preferences = prefsState,
                                hasNotificationPermission = hasNotificationPermission,
                                hasGalleryStorageAccess = hasGalleryStorageAccess,
                                onRequestAllPermissions = {
                                    requestAllRuntimePermissions()
                                },
                                onOpenFullFilePicker = {
                                    pendingDestinationAfterImport = PrimaryScreen.EnhanceVideo
                                    openDocumentLauncher.launch(arrayOf("video/*"))
                                },
                                onSelectPalette = { palette ->
                                    viewModel.setThemePalette(palette)
                                },
                                onSelectThemeMode = { mode ->
                                    viewModel.setThemeMode(mode)
                                },
                                onSelectFontFamily = { font ->
                                    viewModel.setFontFamily(font)
                                },
                                onSelectRoute = { route ->
                                    viewModel.setDefaultRoute(route)
                                },
                                onTogglePreserveMetadata = { preserve ->
                                    viewModel.setPreserveMetadata(preserve)
                                },
                                onToggleNotifications = { enabled ->
                                    viewModel.setNotificationsEnabled(enabled)
                                    if (enabled) {
                                        requestAllRuntimePermissions()
                                    }
                                },
                                onSignOutOrSignInWithGoogle = onSignOutOrExitGuest,
                                onBackToHome = {
                                    viewModel.navigateTo(PrimaryScreen.Home)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (editorState.showVideoPlayerDialog && !editorState.playbackUriString.isNullOrBlank()) {
        VideoPlaybackModalDialog(
            uriString = editorState.playbackUriString!!,
            onDismiss = { viewModel.closeVideoPlayer() }
        )
    }

    if (editorState.showCloudConsentDialog) {
        CloudConsentDialog(
            onConfirmCloud = { rememberChoice ->
                viewModel.confirmCloudConsentAndStart(context, rememberChoice)
            },
            onRunLocallyInstead = {
                viewModel.dismissCloudConsentAndRunLocally(context)
            },
            onDismiss = {
                viewModel.dismissCloudConsentAndRunLocally(context)
            }
        )
    }
}
