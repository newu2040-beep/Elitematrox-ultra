package com.example.feature.editor

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Forward5
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Replay5
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material.icons.outlined.CropRotate
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Exposure
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.core.media.ProcessedVideoOutput
import com.example.core.media.VideoProcessingEngine
import com.example.domain.model.AspectRatioMode
import com.example.domain.model.CinematicColorProfile
import com.example.domain.model.DeviceHardwareCapabilities
import com.example.domain.model.EnhancementAdjustments
import com.example.domain.model.ExportConfiguration
import com.example.domain.model.ProcessingPerformanceMode
import com.example.domain.model.SocialMediaPreset
import com.example.domain.model.VideoMetadata
import com.example.ui.theme.EmeraldPillButton
import com.example.ui.theme.LocalNeumorphicColors
import com.example.ui.theme.NeumorphicAdjustmentSliderRow
import com.example.ui.theme.NeumorphicCard
import com.example.ui.theme.NeumorphicChoicePill
import com.example.ui.theme.NeumorphicPillButton
import com.example.ui.theme.ProcessingCompleteCheckmarkHero
import com.example.ui.theme.ProcessingPerformanceModeSelector
import com.example.ui.theme.SocialPresetBadgeIcon
import com.example.ui.theme.VideoThumbnailPreview
import com.example.ui.theme.neumorphicInset
import com.example.ui.theme.neumorphicRaised
import com.example.ui.theme.rememberResponsiveMetrics
import com.example.viewmodel.EliteMatrixViewModel
import java.io.File
import kotlin.math.abs
import kotlinx.coroutines.delay

/**
 * Screen 3 — Enhance Video (Exact reference mockup implementation + professional color grading,
 * orientation & aspect-ratio framing controls).
 */
@Composable
fun EnhanceVideoScreen(
    selectedVideo: VideoMetadata?,
    thumbnailBitmap: Bitmap?,
    adjustments: EnhancementAdjustments,
    exportConfig: ExportConfiguration,
    onUpdateAdjustments: ((EnhancementAdjustments) -> EnhancementAdjustments) -> Unit,
    onApplyCinematicProfile: (CinematicColorProfile) -> Unit = {},
    onResetAdjustments: () -> Unit,
    onSavePreset: (String) -> Unit,
    onImportNewVideo: () -> Unit,
    onLoadSampleVideo: () -> Unit,
    onOpenFullVideoPlayer: (String) -> Unit,
    onApplyChangesNavigateToExport: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val neu = LocalNeumorphicColors.current
    val metrics = rememberResponsiveMetrics()
    var showOverflowMenu by remember { mutableStateOf(false) }
    var showAdvancedOptions by rememberSaveable { mutableStateOf(false) }
    var compareOriginal by rememberSaveable { mutableStateOf(false) }
    var showSavePresetDialog by rememberSaveable { mutableStateOf(false) }
    var isPreviewPlaying by rememberSaveable { mutableStateOf(false) }

    val (targetW, targetH) = exportConfig.targetDimensionsPx(
        sourceWidth = selectedVideo?.displayWidth ?: 1920,
        sourceHeight = selectedVideo?.displayHeight ?: 1080,
        manualRotationDegrees = adjustments.rotationDegrees,
        aspectRatioMode = adjustments.aspectRatioMode
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = metrics.horizontalPadding, vertical = metrics.verticalPadding)
    ) {
        // 1. Top Bar: Back arrow, "Enhance Video", 3-dot overflow menu
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("enhance_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = neu.textPrimary
                )
            }

            Text(
                text = "Enhance Video",
                color = neu.textPrimary,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            )

            Box {
                IconButton(
                    onClick = { showOverflowMenu = true },
                    modifier = Modifier.testTag("enhance_more_menu_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = neu.textPrimary
                    )
                }
                DropdownMenu(
                    expanded = showOverflowMenu,
                    onDismissRequest = { showOverflowMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(if (compareOriginal) "Show Enhanced Preview" else "Compare Original (Before/After)") },
                        onClick = {
                            showOverflowMenu = false
                            compareOriginal = !compareOriginal
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Import Video from Device") },
                        onClick = {
                            showOverflowMenu = false
                            onImportNewVideo()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Load Sample Clip (Nature_Video.mp4)") },
                        onClick = {
                            showOverflowMenu = false
                            onLoadSampleVideo()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Save Current Adjustments as Preset") },
                        onClick = {
                            showOverflowMenu = false
                            showSavePresetDialog = true
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 2. Video Preview Card (Rounded 22.dp with exact aspect ratio preservation, Play button & Fullscreen icon)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(metrics.videoPreviewHeight)
                .neumorphicRaised(cornerRadius = 22.dp, elevation = 5.dp)
                .clip(RoundedCornerShape(22.dp))
                .clickable {
                    if (selectedVideo != null) {
                        isPreviewPlaying = !isPreviewPlaying
                    } else {
                        onLoadSampleVideo()
                    }
                }
                .testTag("enhance_video_preview_box")
        ) {
            if (isPreviewPlaying && selectedVideo != null) {
                InlineExoPlayerView(
                    uriString = selectedVideo.uriString,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                VideoThumbnailPreview(
                    thumbnailBitmap = thumbnailBitmap,
                    adjustments = if (compareOriginal) EnhancementAdjustments.Neutral else adjustments,
                    preserveAspectRatio = true,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Source Video Spec & Aspect Ratio Badge (Top-Left)
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Black.copy(alpha = 0.64f))
                    .clickable { compareOriginal = !compareOriginal }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (compareOriginal) {
                        "BEFORE (Original)"
                    } else if (selectedVideo != null) {
                        val rotSuffix = if (adjustments.rotationDegrees != 0) " • +${adjustments.rotationDegrees}°" else ""
                        "${selectedVideo.orientationCategoryLabel} • ${targetW}×${targetH}$rotSuffix • ${adjustments.cinematicProfile.label}"
                    } else {
                        "Tap to Preview • ${adjustments.cinematicProfile.label}"
                    },
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.5.sp)
                )
            }

            // Center Play/Pause Circle Button
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .align(Alignment.Center)
                    .clip(CircleShape)
                    .background(Color(0xFF102019).copy(alpha = 0.62f))
                    .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                    .clickable {
                        if (selectedVideo != null) {
                            isPreviewPlaying = !isPreviewPlaying
                        } else {
                            onLoadSampleVideo()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPreviewPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isPreviewPlaying) "Pause Preview" else "Play Preview",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Bottom-Right Fullscreen Expand Button
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp)
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF102019).copy(alpha = 0.62f))
                    .clickable {
                        if (selectedVideo != null) {
                            onOpenFullVideoPlayer(selectedVideo.uriString)
                        } else {
                            onLoadSampleVideo()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Fullscreen,
                    contentDescription = "Fullscreen Preview",
                    tint = Color.White,
                    modifier = Modifier.size(19.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Cinematic Color Profiles Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CinematicColorProfile.entries.forEach { profile ->
                val isSelected = adjustments.cinematicProfile == profile
                Box(
                    modifier = Modifier
                        .height(32.dp)
                        .then(
                            if (isSelected) {
                                Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(neu.accentPrimary, neu.accentSecondary)
                                        )
                                    )
                            } else {
                                Modifier.neumorphicRaised(cornerRadius = 16.dp, elevation = 2.dp)
                            }
                        )
                        .clickable { onApplyCinematicProfile(profile) }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = profile.label,
                        color = if (isSelected) Color.White else neu.textPrimary,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.5.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Compact Aspect Ratio, Framing & Rotation Strip (Directly Accessible)
        NeumorphicCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 18.dp,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.CropRotate,
                            contentDescription = null,
                            tint = neu.accentPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Aspect Ratio & Orientation",
                            color = neu.textPrimary,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        )
                    }
                    Text(
                        text = "${targetW}×${targetH} • ${if (adjustments.scaleToFillCrop) "Center Crop" else "Fit (No Distortion)"}",
                        color = neu.accentPrimary,
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Aspect Ratio Presets Scroll Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AspectRatioMode.entries.forEach { arMode ->
                        val isSelected = adjustments.aspectRatioMode == arMode
                        Box(
                            modifier = Modifier
                                .height(30.dp)
                                .testTag("aspect_ratio_chip_${arMode.name}")
                                .then(
                                    if (isSelected) {
                                        Modifier
                                            .clip(RoundedCornerShape(15.dp))
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(neu.accentPrimary, neu.accentSecondary)
                                                )
                                            )
                                    } else {
                                        Modifier.neumorphicRaised(cornerRadius = 15.dp, elevation = 2.dp)
                                    }
                                )
                                .clickable { onUpdateAdjustments { it.copy(aspectRatioMode = arMode) } }
                                .padding(horizontal = 11.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = arMode.shortLabel,
                                color = if (isSelected) Color.White else neu.textPrimary,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Rotation (0°, 90°, 180°, 270°) + Framing Mode (Fit vs Crop)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(0, 90, 180, 270).forEach { deg ->
                        NeumorphicChoicePill(
                            label = "${deg}°",
                            selected = adjustments.rotationDegrees == deg,
                            testTag = "rotate_pill_$deg",
                            onClick = { onUpdateAdjustments { it.copy(rotationDegrees = deg) } }
                        )
                    }
                    NeumorphicChoicePill(
                        label = if (adjustments.scaleToFillCrop) "Crop" else "Fit",
                        selected = adjustments.scaleToFillCrop,
                        testTag = "framing_fit_crop_pill",
                        onClick = { onUpdateAdjustments { it.copy(scaleToFillCrop = !it.scaleToFillCrop) } }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Adjustments Neumorphic Card
        NeumorphicCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 22.dp,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header Row: "Adjustments" + "↻ Reset" pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Adjustments",
                        color = neu.textPrimary,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.5.sp
                        )
                    )

                    // Raised "↻ Reset" pill button
                    Box(
                        modifier = Modifier
                            .testTag("reset_adjustments_button")
                            .height(32.dp)
                            .neumorphicRaised(cornerRadius = 16.dp, elevation = 3.dp)
                            .clickable(onClick = onResetAdjustments)
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = "Reset Adjustments",
                                tint = neu.textPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Reset",
                                color = neu.textPrimary,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Slider 1: Brightness (+15)
                NeumorphicAdjustmentSliderRow(
                    icon = Icons.Outlined.WbSunny,
                    iconTint = neu.sunAmber,
                    label = "Brightness",
                    value = adjustments.brightness,
                    onValueChange = { v -> onUpdateAdjustments { it.copy(brightness = v) } },
                    testTag = "slider_brightness"
                )

                // Slider 2: Contrast (+20)
                NeumorphicAdjustmentSliderRow(
                    icon = Icons.Outlined.Contrast,
                    iconTint = neu.textPrimary,
                    label = "Contrast",
                    value = adjustments.contrast,
                    onValueChange = { v -> onUpdateAdjustments { it.copy(contrast = v) } },
                    testTag = "slider_contrast"
                )

                // Slider 3: Saturation (+10)
                NeumorphicAdjustmentSliderRow(
                    icon = Icons.Outlined.WaterDrop,
                    iconTint = neu.dropletMint,
                    label = "Saturation",
                    value = adjustments.saturation,
                    onValueChange = { v -> onUpdateAdjustments { it.copy(saturation = v) } },
                    testTag = "slider_saturation"
                )

                // Slider 4: Temperature (0)
                NeumorphicAdjustmentSliderRow(
                    icon = Icons.Outlined.Thermostat,
                    iconTint = neu.thermoOrange,
                    label = "Temperature",
                    value = adjustments.temperature,
                    onValueChange = { v -> onUpdateAdjustments { it.copy(temperature = v) } },
                    testTag = "slider_temperature"
                )

                Spacer(modifier = Modifier.height(8.dp))

                // "Advanced Options v" expandable pill button inside the card
                NeumorphicPillButton(
                    text = "Advanced Options",
                    height = 40.dp,
                    testTag = "advanced_options_button",
                    trailingContent = {
                        Icon(
                            imageVector = if (showAdvancedOptions) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                            contentDescription = "Toggle Advanced Options",
                            tint = neu.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    onClick = { showAdvancedOptions = !showAdvancedOptions }
                )

                AnimatedVisibility(visible = showAdvancedOptions) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    ) {
                        HorizontalDivider(color = neu.divider)
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Pro Color Grading & Detail",
                            color = neu.accentPrimary,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        // Exposure
                        NeumorphicAdjustmentSliderRow(
                            icon = Icons.Outlined.Exposure,
                            iconTint = neu.sunAmber,
                            label = "Exposure",
                            value = adjustments.exposure,
                            onValueChange = { v -> onUpdateAdjustments { it.copy(exposure = v) } },
                            testTag = "slider_exposure"
                        )

                        // Vibrance
                        NeumorphicAdjustmentSliderRow(
                            icon = Icons.Outlined.AutoAwesome,
                            iconTint = neu.dropletMint,
                            label = "Vibrance",
                            value = adjustments.vibrance,
                            onValueChange = { v -> onUpdateAdjustments { it.copy(vibrance = v) } },
                            testTag = "slider_vibrance"
                        )

                        // Highlights
                        NeumorphicAdjustmentSliderRow(
                            icon = Icons.Outlined.WbSunny,
                            iconTint = neu.accentPrimary,
                            label = "Highlights",
                            value = adjustments.highlights,
                            onValueChange = { v -> onUpdateAdjustments { it.copy(highlights = v) } },
                            testTag = "slider_highlights"
                        )

                        // Shadows
                        NeumorphicAdjustmentSliderRow(
                            icon = Icons.Outlined.Contrast,
                            iconTint = neu.textSecondary,
                            label = "Shadows",
                            value = adjustments.shadows,
                            onValueChange = { v -> onUpdateAdjustments { it.copy(shadows = v) } },
                            testTag = "slider_shadows"
                        )

                        // Tint (Green / Magenta)
                        NeumorphicAdjustmentSliderRow(
                            icon = Icons.Outlined.Palette,
                            iconTint = neu.thermoOrange,
                            label = "Tint (G/M)",
                            value = adjustments.tint,
                            onValueChange = { v -> onUpdateAdjustments { it.copy(tint = v) } },
                            testTag = "slider_tint"
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Sharpening (unsharp filter)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Sharpness (Luma Unsharp Mask)", style = MaterialTheme.typography.bodyMedium, color = neu.textPrimary)
                            Text("${adjustments.sharpness}%", style = MaterialTheme.typography.labelLarge, color = neu.accentPrimary)
                        }
                        Slider(
                            value = adjustments.sharpness.toFloat(),
                            onValueChange = { v -> onUpdateAdjustments { it.copy(sharpness = v.toInt()) } },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(activeTrackColor = neu.accentPrimary)
                        )

                        // Denoising (hqdn3d filter)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Temporal/Spatial Denoise", style = MaterialTheme.typography.bodyMedium, color = neu.textPrimary)
                            Text("${adjustments.denoise}%", style = MaterialTheme.typography.labelLarge, color = neu.accentPrimary)
                        }
                        Slider(
                            value = adjustments.denoise.toFloat(),
                            onValueChange = { v -> onUpdateAdjustments { it.copy(denoise = v.toInt()) } },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(activeTrackColor = neu.accentPrimary)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Live FFmpeg Filter Graph Preview
                        val cmdPreview = remember(adjustments, exportConfig, selectedVideo) {
                            VideoProcessingEngine.buildFfmpegCommand(
                                inputPath = selectedVideo?.fileName ?: "Nature_Video.mp4",
                                outputPath = "output.mp4",
                                adjustments = adjustments,
                                exportConfig = exportConfig,
                                sourceWidth = selectedVideo?.displayWidth ?: 1920,
                                sourceHeight = selectedVideo?.displayHeight ?: 1080
                            ).joinToString(" ")
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .neumorphicInset(cornerRadius = 12.dp)
                                .padding(10.dp)
                        ) {
                            Text(
                                text = cmdPreview,
                                color = neu.textSecondary,
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.5.sp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Bottom CTA Button: "⚡ Apply Changes"
        EmeraldPillButton(
            text = "Apply Changes",
            testTag = "apply_changes_button",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Bolt,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            },
            onClick = onApplyChangesNavigateToExport
        )

        Spacer(modifier = Modifier.height(10.dp))
    }

    if (showSavePresetDialog) {
        SavePresetDialog(
            onDismiss = { showSavePresetDialog = false },
            onConfirmSave = { name ->
                showSavePresetDialog = false
                onSavePreset(name)
            }
        )
    }
}

/**
 * Screen 4 — Export Settings (Exact reference mockup implementation + LITE/ELITE/MAX performance modes,
 * 50/60/90/120 FPS, Hardware Display Hz detection, Audio Export options, and Social Media Anti-Recompression Engine).
 */
@Composable
fun ExportSettingsScreen(
    selectedVideo: VideoMetadata?,
    adjustments: EnhancementAdjustments = EnhancementAdjustments(),
    exportConfig: ExportConfiguration,
    deviceCapabilities: DeviceHardwareCapabilities = DeviceHardwareCapabilities(),
    isProcessing: Boolean,
    isPaused: Boolean = false,
    processingProgress: Int,
    processingStatus: String,
    elapsedTimeMs: Long = 0L,
    estimatedRemainingMs: Long = 0L,
    onUpdateExportConfig: ((ExportConfiguration) -> ExportConfiguration) -> Unit,
    onSelectPerformanceMode: (ProcessingPerformanceMode) -> Unit = {},
    onApplyRecommendedHardwareSettings: () -> Unit = {},
    onSelectSocialPreset: (SocialMediaPreset) -> Unit,
    onSavePreset: (String) -> Unit,
    onStartExportJob: () -> Unit,
    onPauseProcessing: () -> Unit = {},
    onResumeProcessing: () -> Unit = {},
    onCancelProcessing: () -> Unit = {},
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val neu = LocalNeumorphicColors.current
    val metrics = rememberResponsiveMetrics()
    var showCustomBitrateEditor by rememberSaveable { mutableStateOf(false) }
    var showSocialPresetSheet by rememberSaveable { mutableStateOf(false) }
    var showAdvancedExportControls by rememberSaveable { mutableStateOf(false) }
    var showSavePresetDialog by rememberSaveable { mutableStateOf(false) }

    val (targetW, targetH) = exportConfig.targetDimensionsPx(
        sourceWidth = selectedVideo?.displayWidth ?: 1920,
        sourceHeight = selectedVideo?.displayHeight ?: 1080,
        manualRotationDegrees = adjustments.rotationDegrees,
        aspectRatioMode = adjustments.aspectRatioMode
    )
    val effectiveBitrateKbps = exportConfig.effectiveBitrateKbps(isCompressionWorkflow = false)
    val effectiveMbps = "%.1f".format(effectiveBitrateKbps / 1000f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = metrics.horizontalPadding, vertical = metrics.verticalPadding)
    ) {
        // 1. Top Bar: Back arrow, "Export Settings", Save floppy icon
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("export_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = neu.textPrimary
                )
            }

            Text(
                text = "Export Settings",
                color = neu.textPrimary,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            )

            IconButton(
                onClick = { showSavePresetDialog = true },
                modifier = Modifier.testTag("save_preset_top_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Save,
                    contentDescription = "Save Export Preset",
                    tint = neu.textPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Hardware & Display Refresh Rate Intelligence Banner
        NeumorphicCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 16.dp,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 9.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Speed,
                        contentDescription = null,
                        tint = neu.accentPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "${deviceCapabilities.displayRefreshRateHz}Hz Display • HW ${deviceCapabilities.supportedCodecs.joinToString("/")} Ready",
                            color = neu.textPrimary,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp
                            )
                        )
                        Text(
                            text = "Output: ${targetW}×${targetH} (${adjustments.aspectRatioMode.shortLabel}) • ${exportConfig.fps} FPS • $effectiveMbps Mbps (${exportConfig.performanceMode.badgeLabel})",
                            color = neu.accentPrimary,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 10.5.sp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .height(28.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(neu.accentPrimary.copy(alpha = 0.14f))
                        .clickable(onClick = onApplyRecommendedHardwareSettings)
                        .padding(horizontal = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Match ${deviceCapabilities.displayRefreshRateHz}Hz",
                        color = neu.accentPrimary,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // LITE / ELITE / MAX Processing Performance Mode Selector
        Text(
            text = "Processing Performance Mode",
            color = neu.textPrimary,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        ProcessingPerformanceModeSelector(
            selectedMode = exportConfig.performanceMode,
            onSelectMode = { mode ->
                onSelectPerformanceMode(mode)
                onUpdateExportConfig { it.copy(performanceMode = mode) }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Resolution ("720p", "1080p", "2K", "4K")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Resolution",
                color = neu.textPrimary,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            )
            Text(
                text = "${targetW} × ${targetH} px",
                color = neu.textSecondary,
                style = MaterialTheme.typography.labelMedium
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf("720p", "1080p", "2K", "4K").forEach { res ->
                NeumorphicChoicePill(
                    label = res,
                    selected = exportConfig.resolution == res,
                    testTag = "res_pill_$res",
                    onClick = { onUpdateExportConfig { it.copy(resolution = res) } }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Frame Rate ("24", "30", "60" + High-Refresh "50", "90", "120" FPS)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Frame Rate",
                color = neu.textPrimary,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            )
            Text(
                text = "${exportConfig.fps} FPS Constant (CFR)",
                color = neu.accentPrimary,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf(24, 30, 60).forEach { fpsVal ->
                NeumorphicChoicePill(
                    label = fpsVal.toString(),
                    selected = exportConfig.fps == fpsVal,
                    testTag = "fps_pill_$fpsVal",
                    onClick = { onUpdateExportConfig { it.copy(fps = fpsVal) } }
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf(50, 90, 120).forEach { fpsVal ->
                NeumorphicChoicePill(
                    label = "$fpsVal FPS",
                    selected = exportConfig.fps == fpsVal,
                    testTag = "fps_pill_$fpsVal",
                    onClick = { onUpdateExportConfig { it.copy(fps = fpsVal) } }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 4. Video Codec ("H.264", "H.265", "AV1")
        Text(
            text = "Video Codec",
            color = neu.textPrimary,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf("H.264", "H.265", "AV1").forEach { codecName ->
                NeumorphicChoicePill(
                    label = codecName,
                    selected = exportConfig.codec == codecName,
                    testTag = "codec_pill_$codecName",
                    onClick = { onUpdateExportConfig { it.copy(codec = codecName) } }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 5. Bitrate ("Auto" pill + "Custom       >" wide pill)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Bitrate",
                color = neu.textPrimary,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            )
            Text(
                text = "$effectiveMbps Mbps (${exportConfig.bitrateMode} • ${exportConfig.performanceMode.badgeLabel})",
                color = neu.textSecondary,
                style = MaterialTheme.typography.labelMedium
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NeumorphicChoicePill(
                label = "Auto",
                selected = exportConfig.bitrateMode == "Auto",
                testTag = "bitrate_pill_auto",
                onClick = {
                    showCustomBitrateEditor = false
                    onUpdateExportConfig { it.copy(bitrateMode = "Auto") }
                }
            )

            // Wide "Custom   >" pill matching Screen 4 mockup
            val isCustom = exportConfig.bitrateMode == "Custom"
            Box(
                modifier = Modifier
                    .weight(1.9f)
                    .height(40.dp)
                    .testTag("bitrate_pill_custom")
                    .then(
                        if (isCustom) {
                            Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(neu.accentPrimary, neu.accentSecondary)
                                    )
                                )
                        } else {
                            Modifier.neumorphicRaised(cornerRadius = 20.dp, elevation = 3.dp)
                        }
                    )
                    .clickable {
                        onUpdateExportConfig { it.copy(bitrateMode = "Custom") }
                        showCustomBitrateEditor = !showCustomBitrateEditor
                    }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isCustom) {
                            "Custom (${"%.1f".format(exportConfig.customBitrateKbps / 1000f)} Mbps)"
                        } else {
                            "Custom"
                        },
                        color = if (isCustom) Color.White else neu.textPrimary,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.5.sp
                        )
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Configure custom bitrate",
                        tint = if (isCustom) Color.White else neu.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        AnimatedVisibility(visible = showCustomBitrateEditor || exportConfig.bitrateMode == "Custom") {
            NeumorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                cornerRadius = 18.dp,
                contentPadding = PaddingValues(12.dp)
            ) {
                val durationSec = ((selectedVideo?.durationMs ?: 15_000L) / 1000.0).coerceAtLeast(1.0)
                val estBytes = ((effectiveBitrateKbps + exportConfig.audioBitrateKbps) * 1000L / 8.0 * durationSec * 1.02).toLong()
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Target: ${exportConfig.customBitrateKbps} kbps (${"%.1f".format(exportConfig.customBitrateKbps / 1000f)} Mbps)",
                            style = MaterialTheme.typography.labelLarge,
                            color = neu.textPrimary
                        )
                        Text(
                            text = "Est. ~${EliteMatrixViewModel.formatBytes(estBytes)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = neu.accentPrimary
                        )
                    }
                    Slider(
                        value = exportConfig.customBitrateKbps.toFloat(),
                        onValueChange = { v ->
                            onUpdateExportConfig {
                                it.copy(bitrateMode = "Custom", customBitrateKbps = v.toInt())
                            }
                        },
                        valueRange = 1500f..65000f,
                        colors = SliderDefaults.colors(activeTrackColor = neu.accentPrimary)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 6. Format ("MP4", "MOV", "MKV")
        Text(
            text = "Format",
            color = neu.textPrimary,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf("MP4", "MOV", "MKV").forEach { fmt ->
                NeumorphicChoicePill(
                    label = fmt,
                    selected = exportConfig.containerFormat == fmt,
                    testTag = "format_pill_$fmt",
                    onClick = { onUpdateExportConfig { it.copy(containerFormat = fmt) } }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = neu.divider)
        Spacer(modifier = Modifier.height(10.dp))

        // 7. Social Media Presets & Anti-Recompression Engine
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Social Media Presets",
                color = neu.textPrimary,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            )
            Text(
                text = if (showAdvancedExportControls) "Hide Audio & Encoder" else "Audio & Encoder",
                color = neu.accentPrimary,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier
                    .clickable { showAdvancedExportControls = !showAdvancedExportControls }
                    .padding(4.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        val activeSocial = exportConfig.selectedSocialPreset
        NeumorphicCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("social_preset_card"),
            cornerRadius = 18.dp,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
            onClick = { showSocialPresetSheet = !showSocialPresetSheet }
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SocialPresetBadgeIcon(presetName = activeSocial.title)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = activeSocial.title,
                            color = neu.textPrimary,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = activeSocial.subtitle,
                            color = neu.textSecondary,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp)
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Choose Social Media Preset",
                        tint = neu.textSecondary
                    )
                }
                if (exportConfig.socialOptimizationEnabled) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "✓ Anti-Recompression Guard: ${activeSocial.antiCompressionNote}",
                        color = neu.accentPrimary,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 10.5.sp)
                    )
                }
            }
        }

        AnimatedVisibility(visible = showSocialPresetSheet) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SocialMediaPreset.entries.forEach { preset ->
                    NeumorphicCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 16.dp,
                        contentPadding = PaddingValues(11.dp),
                        onClick = {
                            onSelectSocialPreset(preset)
                            showSocialPresetSheet = false
                        }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SocialPresetBadgeIcon(presetName = preset.title, modifier = Modifier.size(30.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(preset.title, style = MaterialTheme.typography.titleMedium, color = neu.textPrimary)
                                Text(
                                    "${preset.subtitle} • ${"%.1f".format(preset.bitrateKbps / 1000f)} Mbps",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                                    color = neu.accentPrimary
                                )
                                Text(
                                    preset.antiCompressionNote,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 10.5.sp),
                                    color = neu.textSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(visible = showAdvancedExportControls) {
            NeumorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                cornerRadius = 18.dp,
                contentPadding = PaddingValues(12.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.GraphicEq,
                            contentDescription = null,
                            tint = neu.accentPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Master Audio & Encoder Studio",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = neu.textPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Audio Codec
                    Text("Audio Codec", style = MaterialTheme.typography.labelLarge, color = neu.textPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("AAC-LC", "HE-AAC", "Opus").forEach { aCodec ->
                            NeumorphicChoicePill(
                                label = aCodec,
                                selected = exportConfig.audioCodec == aCodec,
                                onClick = { onUpdateExportConfig { it.copy(audioCodec = aCodec) } }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Audio Bitrate
                    Text("Audio Bitrate (kbps)", style = MaterialTheme.typography.labelLarge, color = neu.textPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(128, 192, 256, 320).forEach { aBitrate ->
                            NeumorphicChoicePill(
                                label = "${aBitrate}k",
                                selected = exportConfig.audioBitrateKbps == aBitrate,
                                onClick = { onUpdateExportConfig { it.copy(audioBitrateKbps = aBitrate) } }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Audio Sample Rate
                    Text("Audio Sample Rate", style = MaterialTheme.typography.labelLarge, color = neu.textPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(44100 to "44.1 kHz", 48000 to "48 kHz").forEach { (hz, label) ->
                            NeumorphicChoicePill(
                                label = label,
                                selected = exportConfig.audioSampleRateHz == hz,
                                onClick = { onUpdateExportConfig { it.copy(audioSampleRateHz = hz) } }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = neu.divider)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Social Anti-Recompression Lock Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Social Anti-Recompression Guard",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = neu.textPrimary
                            )
                            Text(
                                "Locks CFR, BT.709 color & 2s GOP to prevent upload quality loss",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                                color = neu.textSecondary
                            )
                        }
                        Switch(
                            checked = exportConfig.socialOptimizationEnabled,
                            onCheckedChange = { checked ->
                                onUpdateExportConfig { it.copy(socialOptimizationEnabled = checked) }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Constant Rate Factor (CRF)", style = MaterialTheme.typography.bodyMedium, color = neu.textPrimary)
                        Text("CRF ${exportConfig.crf}", style = MaterialTheme.typography.labelLarge, color = neu.accentPrimary)
                    }
                    Slider(
                        value = exportConfig.crf.toFloat(),
                        onValueChange = { v -> onUpdateExportConfig { it.copy(crf = v.toInt()) } },
                        valueRange = 14f..32f,
                        colors = SliderDefaults.colors(activeTrackColor = neu.accentPrimary)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Preserve Color & Source Metadata", style = MaterialTheme.typography.bodyMedium, color = neu.textPrimary)
                        Switch(
                            checked = exportConfig.preserveMetadata,
                            onCheckedChange = { checked ->
                                onUpdateExportConfig { it.copy(preserveMetadata = checked) }
                            }
                        )
                    }
                }
            }
        }

        if (isProcessing) {
            Spacer(modifier = Modifier.height(12.dp))
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 18.dp,
                contentPadding = PaddingValues(12.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = processingStatus,
                                style = MaterialTheme.typography.labelLarge,
                                color = neu.textPrimary
                            )
                            Text(
                                text = "Elapsed: ${EliteMatrixViewModel.formatDurationSeconds(elapsedTimeMs)} • ETA: ${if (estimatedRemainingMs > 0L) EliteMatrixViewModel.formatDurationSeconds(estimatedRemainingMs) else "Calculating..."}",
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                                color = neu.textSecondary
                            )
                        }
                        Text("$processingProgress%", style = MaterialTheme.typography.labelLarge, color = neu.accentPrimary)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { processingProgress / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (isPaused) neu.sunAmber else neu.accentPrimary,
                        trackColor = neu.surfaceInset
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NeumorphicPillButton(
                            text = if (isPaused) "Resume" else "Pause",
                            height = 36.dp,
                            leadingContent = {
                                Icon(
                                    imageVector = if (isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                                    contentDescription = null,
                                    tint = neu.accentPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            modifier = Modifier.weight(1f),
                            onClick = { if (isPaused) onResumeProcessing() else onPauseProcessing() }
                        )
                        NeumorphicPillButton(
                            text = "Cancel",
                            height = 36.dp,
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            modifier = Modifier.weight(1f),
                            onClick = onCancelProcessing
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 8. Bottom Primary Emerald CTA Button: "[↑] Export Video"
        EmeraldPillButton(
            text = "Export Video (${exportConfig.performanceMode.badgeLabel})",
            testTag = "export_video_button",
            isLoading = isProcessing,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.FileUpload,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            },
            onClick = onStartExportJob
        )

        Spacer(modifier = Modifier.height(10.dp))
    }

    if (showSavePresetDialog) {
        SavePresetDialog(
            onDismiss = { showSavePresetDialog = false },
            onConfirmSave = { name ->
                showSavePresetDialog = false
                onSavePreset(name)
            }
        )
    }
}

/**
 * Screen 5 — Processing Complete! (Exact reference mockup implementation + Verified Output Spec Card).
 */
@Composable
fun ProcessingCompleteScreen(
    completedOutput: ProcessedVideoOutput?,
    thumbnailBitmap: Bitmap?,
    onOpenVideo: (String) -> Unit,
    onGoToProjects: () -> Unit,
    onBackToHome: () -> Unit
) {
    BackHandler(onBack = onBackToHome)

    val neu = LocalNeumorphicColors.current
    val context = LocalContext.current

    val fileName = completedOutput?.fileName ?: "Nature_Video.mp4"
    val origBytes = completedOutput?.originalSizeBytes ?: 25_690_112L
    val outBytes = completedOutput?.outputSizeBytes ?: 19_608_371L
    val savedPct = completedOutput?.savedPercent ?: 24
    val savingsLabel = if (savedPct >= 0) {
        "$savedPct% smaller"
    } else {
        "${abs(savedPct)}% HQ bitrate boost"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(neu.canvas)
    ) {
        // Bottom soft mint wave artwork matching Screen 5 mockup
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .align(Alignment.BottomCenter)
        ) {
            val w = size.width
            val h = size.height
            val wave = Path().apply {
                moveTo(0f, h * 0.85f)
                cubicTo(
                    w * 0.35f, h * 0.90f,
                    w * 0.58f, h * 0.25f,
                    w, h * 0.45f
                )
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(
                path = wave,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        neu.accentSoftMintStart.copy(alpha = 0.25f),
                        neu.accentSoftMintEnd.copy(alpha = 0.75f)
                    )
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(2.dp))

            // 1. Concentric Mint Halo Checkmark Hero + Title & Subtitle
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ProcessingCompleteCheckmarkHero()
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Processing Complete!",
                    color = neu.textPrimary,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp
                    )
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Your video is ready",
                    color = neu.textSecondary,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Output Summary Card ("Nature_Video.mp4" / "24.5 MB → 18.7 MB" / "🌿 24% smaller") + Verified Spec Table
            NeumorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("processing_complete_summary_card"),
                cornerRadius = 22.dp,
                contentPadding = PaddingValues(14.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 86.dp, height = 64.dp)
                                .clip(RoundedCornerShape(14.dp))
                        ) {
                            VideoThumbnailPreview(
                                thumbnailBitmap = thumbnailBitmap,
                                preserveAspectRatio = true,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = fileName,
                                color = neu.textPrimary,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.5.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "${EliteMatrixViewModel.formatBytes(origBytes)} → ${EliteMatrixViewModel.formatBytes(outBytes)}",
                                color = neu.textSecondary,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.5.sp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Eco,
                                    contentDescription = null,
                                    tint = neu.accentPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = savingsLabel,
                                    color = neu.accentPrimary,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = neu.accentPrimary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Saved to Gallery (${completedOutput?.galleryRelativePath ?: "Movies/EliteMatrix"})",
                                    color = neu.textSecondary,
                                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    if (completedOutput != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = neu.divider)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Verified,
                                contentDescription = null,
                                tint = neu.accentPrimary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Verified Exported File Metadata",
                                color = neu.textPrimary,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        val srcW = completedOutput.sourceMetadata?.displayWidth ?: completedOutput.width
                        val srcH = completedOutput.sourceMetadata?.displayHeight ?: completedOutput.height
                        val srcFps = completedOutput.sourceMetadata?.fps?.toInt() ?: 30
                        val outMbps = "%.1f".format(completedOutput.bitrateKbps / 1000f)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Resolution: ${srcW}×${srcH} → ${completedOutput.width}×${completedOutput.height} (${completedOutput.resolutionLabel})",
                                color = neu.textSecondary,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.5.sp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Frame Rate: ${srcFps} FPS → ${completedOutput.fps} FPS CFR",
                                color = neu.accentPrimary,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp
                                )
                            )
                            Text(
                                text = "Bitrate: $outMbps Mbps",
                                color = neu.textPrimary,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.5.sp
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Streams: ${completedOutput.codec} (${completedOutput.containerFormat}) • ${completedOutput.audioCodec} ${completedOutput.audioBitrateKbps} kbps (${completedOutput.audioSampleRateHz / 1000.0} kHz)",
                            color = neu.textSecondary,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Action Buttons ("▶ Open Video", "<o> Share", "[Folder] Go to Projects")
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                EmeraldPillButton(
                    text = "Open Video",
                    testTag = "open_video_button",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    onClick = {
                        val uriStr = completedOutput?.outputUri?.toString()
                        if (!uriStr.isNullOrBlank()) {
                            onOpenVideo(uriStr)
                        }
                    }
                )

                NeumorphicPillButton(
                    text = "Share",
                    testTag = "share_video_button",
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = null,
                            tint = neu.accentPrimary,
                            modifier = Modifier.size(19.dp)
                        )
                    },
                    onClick = {
                        completedOutput?.outputFile?.let { file ->
                            shareVideoFile(context, file)
                        }
                    }
                )

                NeumorphicPillButton(
                    text = "Go to Projects",
                    testTag = "go_to_projects_button",
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Outlined.Folder,
                            contentDescription = null,
                            tint = neu.accentPrimary,
                            modifier = Modifier.size(19.dp)
                        )
                    },
                    onClick = onGoToProjects
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

fun shareVideoFile(context: Context, file: File) {
    try {
        if (!file.exists()) return
        val authority = "${context.packageName}.fileprovider"
        val contentUri = FileProvider.getUriForFile(context, authority, file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "video/*"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            Intent.createChooser(shareIntent, "Share Exported Video")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    } catch (_: Exception) {
    }
}

@Composable
fun InlineExoPlayerView(
    uriString: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val exoPlayer = remember(uriString) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.parse(uriString)))
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = true
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            }
        },
        update = { view ->
            view.player = exoPlayer
            view.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        },
        modifier = modifier
    )
}

@Composable
fun VideoPlaybackModalDialog(
    uriString: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val neu = LocalNeumorphicColors.current

    var isPlaying by remember { mutableStateOf(true) }
    var isStopped by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }
    var isLooping by remember { mutableStateOf(true) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var currentPosMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(1L) }
    var isUserScrubbing by remember { mutableStateOf(false) }
    var scrubFraction by remember { mutableFloatStateOf(0f) }

    val exoPlayer = remember(uriString) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.parse(uriString)))
            repeatMode = Player.REPEAT_MODE_ONE
            prepare()
            playWhenReady = true
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(playing: Boolean) {
                    isPlaying = playing
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (duration > 0) {
                        durationMs = duration
                    }
                    if (playbackState == Player.STATE_ENDED && repeatMode == Player.REPEAT_MODE_OFF) {
                        isPlaying = false
                    }
                }
            })
        }
    }

    // Real-time progress ticker updating every 80ms
    LaunchedEffect(exoPlayer) {
        while (true) {
            if (!isUserScrubbing) {
                currentPosMs = exoPlayer.currentPosition.coerceAtLeast(0L)
                val dur = exoPlayer.duration
                if (dur > 0L) {
                    durationMs = dur
                }
            }
            isPlaying = exoPlayer.isPlaying
            delay(80L)
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        NeumorphicCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            cornerRadius = 26.dp,
            contentPadding = PaddingValues(18.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Real-Time Video Player",
                            color = neu.textPrimary,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        )
                        Text(
                            text = "Speed: ${playbackSpeed}x • ${if (isPlaying) "Playing" else if (isStopped) "Stopped" else "Paused"}",
                            color = neu.accentPrimary,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    TextButton(onClick = onDismiss) {
                        Text("Done", color = neu.accentPrimary, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Video Surface (RESIZE_MODE_FIT preserves exact portrait/landscape/custom aspect ratio)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.Black)
                ) {
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                player = exoPlayer
                                useController = false
                                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                            }
                        },
                        update = { view ->
                            view.player = exoPlayer
                            view.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Live Seekbar + Time Labels (MM:SS / MM:SS)
                val safeDuration = durationMs.coerceAtLeast(1L)
                val progressValue = if (isUserScrubbing) {
                    scrubFraction
                } else {
                    (currentPosMs.toFloat() / safeDuration.toFloat()).coerceIn(0f, 1f)
                }

                Slider(
                    value = progressValue,
                    onValueChange = { frac ->
                        isUserScrubbing = true
                        scrubFraction = frac
                        currentPosMs = (frac * safeDuration).toLong()
                    },
                    onValueChangeFinished = {
                        val targetMs = (scrubFraction * safeDuration).toLong()
                        exoPlayer.seekTo(targetMs)
                        isUserScrubbing = false
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = neu.accentPrimary,
                        activeTrackColor = neu.accentPrimary,
                        inactiveTrackColor = neu.surfaceInset
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("video_player_seek_slider")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTimestampMs(currentPosMs),
                        color = neu.textPrimary,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = formatTimestampMs(safeDuration),
                        color = neu.textSecondary,
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Primary Transport Controls: Skip -5s | Play/Pause/Resume | Stop | Skip +5s | Mute | Loop
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rewind 5s
                    IconButton(
                        onClick = {
                            val target = (exoPlayer.currentPosition - 5000L).coerceAtLeast(0L)
                            exoPlayer.seekTo(target)
                            currentPosMs = target
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .neumorphicRaised(cornerRadius = 22.dp, elevation = 3.dp)
                            .testTag("player_rewind_5s_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Replay5,
                            contentDescription = "Rewind 5 seconds",
                            tint = neu.textPrimary
                        )
                    }

                    // Play / Pause / Resume Button
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(neu.accentPrimary, neu.accentSecondary)
                                )
                            )
                            .clickable {
                                if (exoPlayer.isPlaying) {
                                    exoPlayer.pause()
                                    isPlaying = false
                                } else {
                                    if (isStopped || exoPlayer.playbackState == Player.STATE_IDLE) {
                                        exoPlayer.prepare()
                                    }
                                    if (exoPlayer.playbackState == Player.STATE_ENDED) {
                                        exoPlayer.seekTo(0L)
                                    }
                                    isStopped = false
                                    exoPlayer.play()
                                    isPlaying = true
                                }
                            }
                            .testTag("player_play_pause_resume_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play or Resume",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    // Stop Button (Pause + Reset to 00:00)
                    IconButton(
                        onClick = {
                            exoPlayer.pause()
                            exoPlayer.seekTo(0L)
                            currentPosMs = 0L
                            isPlaying = false
                            isStopped = true
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .neumorphicRaised(cornerRadius = 22.dp, elevation = 3.dp)
                            .testTag("player_stop_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Stop,
                            contentDescription = "Stop Video",
                            tint = neu.textPrimary
                        )
                    }

                    // Forward 5s
                    IconButton(
                        onClick = {
                            val dur = exoPlayer.duration.coerceAtLeast(1000L)
                            val target = (exoPlayer.currentPosition + 5000L).coerceAtMost(dur)
                            exoPlayer.seekTo(target)
                            currentPosMs = target
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .neumorphicRaised(cornerRadius = 22.dp, elevation = 3.dp)
                            .testTag("player_forward_5s_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Forward5,
                            contentDescription = "Forward 5 seconds",
                            tint = neu.textPrimary
                        )
                    }

                    // Mute / Unmute
                    IconButton(
                        onClick = {
                            isMuted = !isMuted
                            exoPlayer.volume = if (isMuted) 0f else 1f
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .neumorphicRaised(cornerRadius = 22.dp, elevation = 3.dp)
                            .testTag("player_mute_button")
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp,
                            contentDescription = if (isMuted) "Unmute" else "Mute",
                            tint = if (isMuted) neu.thermoOrange else neu.textPrimary
                        )
                    }

                    // Loop Toggle
                    IconButton(
                        onClick = {
                            isLooping = !isLooping
                            exoPlayer.repeatMode = if (isLooping) {
                                Player.REPEAT_MODE_ONE
                            } else {
                                Player.REPEAT_MODE_OFF
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .neumorphicRaised(cornerRadius = 22.dp, elevation = 3.dp)
                            .testTag("player_loop_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Repeat,
                            contentDescription = "Toggle Loop",
                            tint = if (isLooping) neu.accentPrimary else neu.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Playback Speed Selector Row (0.5x, 0.75x, 1.0x, 1.25x, 1.5x, 2.0x)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Speed,
                        contentDescription = null,
                        tint = neu.accentPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Playback Speed",
                        color = neu.textPrimary,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                        val label = if (speed == 1.0f) "1x" else "${speed}x"
                        NeumorphicChoicePill(
                            label = label,
                            selected = playbackSpeed == speed,
                            testTag = "speed_pill_$label",
                            onClick = {
                                playbackSpeed = speed
                                exoPlayer.setPlaybackSpeed(speed)
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun formatTimestampMs(ms: Long): String {
    val totalSeconds = (ms / 1000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "%02d:%02d".format(minutes, seconds)
}

@Composable
fun CloudConsentDialog(
    onConfirmCloud: (rememberChoice: Boolean) -> Unit,
    onRunLocallyInstead: () -> Unit,
    onDismiss: () -> Unit
) {
    var rememberChoice by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cloud Processing Consent") },
        text = {
            Column {
                Text(
                    "This high-complexity job can run on the Google Cloud Run FFmpeg worker using a private signed URL, or locally on your device. Do you consent to uploading this video clip for cloud processing?"
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { rememberChoice = !rememberChoice }
                ) {
                    Checkbox(
                        checked = rememberChoice,
                        onCheckedChange = { rememberChoice = it }
                    )
                    Text("Remember my consent for future jobs")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirmCloud(rememberChoice) }) {
                Text("Allow Cloud Processing")
            }
        },
        dismissButton = {
            TextButton(onClick = onRunLocallyInstead) {
                Text("Process Locally Only")
            }
        }
    )
}

@Composable
fun SavePresetDialog(
    onDismiss: () -> Unit,
    onConfirmSave: (String) -> Unit
) {
    var presetName by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save Custom Preset") },
        text = {
            OutlinedTextField(
                value = presetName,
                onValueChange = { presetName = it },
                label = { Text("Preset Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirmSave(presetName.ifBlank { "My HQ Preset" }) }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
