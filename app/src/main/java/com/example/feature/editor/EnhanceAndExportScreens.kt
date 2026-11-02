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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Thermostat
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
import androidx.compose.runtime.getValue
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
import androidx.core.content.FileProvider
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.core.media.ProcessedVideoOutput
import com.example.core.media.VideoProcessingEngine
import com.example.domain.model.EnhancementAdjustments
import com.example.domain.model.ExportConfiguration
import com.example.domain.model.SocialMediaPreset
import com.example.domain.model.VideoMetadata
import com.example.ui.theme.EmeraldPillButton
import com.example.ui.theme.LocalNeumorphicColors
import com.example.ui.theme.NeumorphicAdjustmentSliderRow
import com.example.ui.theme.NeumorphicCard
import com.example.ui.theme.NeumorphicChoicePill
import com.example.ui.theme.NeumorphicPillButton
import com.example.ui.theme.ProcessingCompleteCheckmarkHero
import com.example.ui.theme.SocialPresetBadgeIcon
import com.example.ui.theme.VideoThumbnailPreview
import com.example.ui.theme.neumorphicInset
import com.example.ui.theme.neumorphicRaised
import com.example.viewmodel.EliteMatrixViewModel
import java.io.File
import kotlin.math.abs

/**
 * Screen 3 — Enhance Video (Exact reference mockup implementation).
 */
@Composable
fun EnhanceVideoScreen(
    selectedVideo: VideoMetadata?,
    thumbnailBitmap: Bitmap?,
    adjustments: EnhancementAdjustments,
    exportConfig: ExportConfiguration,
    onUpdateAdjustments: ((EnhancementAdjustments) -> EnhancementAdjustments) -> Unit,
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
    var showOverflowMenu by remember { mutableStateOf(false) }
    var showAdvancedOptions by rememberSaveable { mutableStateOf(false) }
    var compareOriginal by rememberSaveable { mutableStateOf(false) }
    var showSavePresetDialog by rememberSaveable { mutableStateOf(false) }
    var isPreviewPlaying by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 10.dp)
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

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Video Preview Card (Rounded 22.dp with centered translucent Play button & bottom-right Fullscreen icon)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(216.dp)
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
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Before/After badge when comparing
            if (compareOriginal) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black.copy(alpha = 0.55f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "BEFORE (Original)",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            // Center Play/Pause Circle Button
            Box(
                modifier = Modifier
                    .size(56.dp)
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
                    modifier = Modifier.size(30.dp)
                )
            }

            // Bottom-Right Fullscreen Expand Button
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
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
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 3. Adjustments Neumorphic Card
        NeumorphicCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 24.dp,
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp)
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
                            fontSize = 17.sp
                        )
                    )

                    // Raised "↻ Reset" pill button
                    Box(
                        modifier = Modifier
                            .testTag("reset_adjustments_button")
                            .height(34.dp)
                            .neumorphicRaised(cornerRadius = 17.dp, elevation = 3.dp)
                            .clickable(onClick = onResetAdjustments)
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = "Reset Adjustments",
                                tint = neu.textPrimary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Reset",
                                color = neu.textPrimary,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.5.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

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

                Spacer(modifier = Modifier.height(10.dp))

                // "Advanced Options v" expandable pill button inside the card
                NeumorphicPillButton(
                    text = "Advanced Options",
                    height = 42.dp,
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
                            .padding(top = 14.dp)
                    ) {
                        HorizontalDivider(color = neu.divider)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Sharpening (unsharp filter)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Sharpening (Unsharp Mask)", style = MaterialTheme.typography.bodyMedium, color = neu.textPrimary)
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

                        // Rotation selector
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Rotation", style = MaterialTheme.typography.bodyMedium, color = neu.textPrimary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(0, 90, 180, 270).forEach { deg ->
                                NeumorphicChoicePill(
                                    label = "${deg}°",
                                    selected = adjustments.rotationDegrees == deg,
                                    onClick = { onUpdateAdjustments { it.copy(rotationDegrees = deg) } }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Live FFmpeg Filter Graph Preview
                        val cmdPreview = remember(adjustments, exportConfig) {
                            VideoProcessingEngine.buildFfmpegCommand(
                                inputPath = selectedVideo?.fileName ?: "Nature_Video.mp4",
                                outputPath = "output.mp4",
                                adjustments = adjustments,
                                exportConfig = exportConfig
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
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

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

        Spacer(modifier = Modifier.height(14.dp))
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
 * Screen 4 — Export Settings (Exact reference mockup implementation).
 */
@Composable
fun ExportSettingsScreen(
    selectedVideo: VideoMetadata?,
    exportConfig: ExportConfiguration,
    isProcessing: Boolean,
    processingProgress: Int,
    processingStatus: String,
    onUpdateExportConfig: ((ExportConfiguration) -> ExportConfiguration) -> Unit,
    onSelectSocialPreset: (SocialMediaPreset) -> Unit,
    onSavePreset: (String) -> Unit,
    onStartExportJob: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val neu = LocalNeumorphicColors.current
    var showCustomBitrateEditor by rememberSaveable { mutableStateOf(false) }
    var showSocialPresetSheet by rememberSaveable { mutableStateOf(false) }
    var showAdvancedExportControls by rememberSaveable { mutableStateOf(false) }
    var showSavePresetDialog by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 10.dp)
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

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Resolution ("720p", "1080p", "2K", "4K")
        Text(
            text = "Resolution",
            color = neu.textPrimary,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 14.5.sp
            )
        )
        Spacer(modifier = Modifier.height(10.dp))
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

        Spacer(modifier = Modifier.height(18.dp))

        // 3. Frame Rate ("24", "30", "60")
        Text(
            text = "Frame Rate",
            color = neu.textPrimary,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 14.5.sp
            )
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
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

        Spacer(modifier = Modifier.height(18.dp))

        // 4. Video Codec ("H.264", "H.265", "AV1")
        Text(
            text = "Video Codec",
            color = neu.textPrimary,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 14.5.sp
            )
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
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

        Spacer(modifier = Modifier.height(18.dp))

        // 5. Bitrate ("Auto" pill + "Custom       >" wide pill)
        Text(
            text = "Bitrate",
            color = neu.textPrimary,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 14.5.sp
            )
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
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
                    .height(42.dp)
                    .testTag("bitrate_pill_custom")
                    .then(
                        if (isCustom) {
                            Modifier
                                .clip(RoundedCornerShape(22.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(neu.accentPrimary, neu.accentSecondary)
                                    )
                                )
                        } else {
                            Modifier.neumorphicRaised(cornerRadius = 22.dp, elevation = 3.dp)
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
                            "Custom (${exportConfig.customBitrateKbps / 1000} Mbps)"
                        } else {
                            "Custom"
                        },
                        color = if (isCustom) Color.White else neu.textPrimary,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
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
                    .padding(top = 12.dp),
                cornerRadius = 18.dp,
                contentPadding = PaddingValues(14.dp)
            ) {
                val durationSec = ((selectedVideo?.durationMs ?: 15_000L) / 1000.0).coerceAtLeast(1.0)
                val estBytes = ((exportConfig.effectiveBitrateKbps() + exportConfig.audioBitrateKbps) * 1000L / 8.0 * durationSec * 1.03).toLong()
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Target Bitrate: ${exportConfig.customBitrateKbps} kbps",
                            style = MaterialTheme.typography.labelLarge,
                            color = neu.textPrimary
                        )
                        Text(
                            text = "Est. Size: ~${EliteMatrixViewModel.formatBytes(estBytes)}",
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
                        valueRange = 1000f..50000f,
                        colors = SliderDefaults.colors(activeTrackColor = neu.accentPrimary)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 6. Format ("MP4", "MOV", "MKV")
        Text(
            text = "Format",
            color = neu.textPrimary,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 14.5.sp
            )
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
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

        Spacer(modifier = Modifier.height(18.dp))
        HorizontalDivider(color = neu.divider)
        Spacer(modifier = Modifier.height(16.dp))

        // 7. Social Media Presets ("TikTok / 1080 × 1920 • 30 FPS   >")
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
                    fontSize = 14.5.sp
                )
            )
            Text(
                text = if (showAdvancedExportControls) "Hide Advanced" else "Advanced",
                color = neu.accentPrimary,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier
                    .clickable { showAdvancedExportControls = !showAdvancedExportControls }
                    .padding(4.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        val activeSocial = exportConfig.selectedSocialPreset
        NeumorphicCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("social_preset_card"),
            cornerRadius = 20.dp,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            onClick = { showSocialPresetSheet = !showSocialPresetSheet }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SocialPresetBadgeIcon(presetName = activeSocial.title)
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = activeSocial.title,
                        color = neu.textPrimary,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = activeSocial.subtitle,
                        color = neu.textSecondary,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.5.sp)
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Choose Social Media Preset",
                    tint = neu.textSecondary
                )
            }
        }

        AnimatedVisibility(visible = showSocialPresetSheet) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SocialMediaPreset.entries.forEach { preset ->
                    NeumorphicCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 16.dp,
                        contentPadding = PaddingValues(12.dp),
                        onClick = {
                            onSelectSocialPreset(preset)
                            showSocialPresetSheet = false
                        }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SocialPresetBadgeIcon(presetName = preset.title, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(preset.title, style = MaterialTheme.typography.titleMedium, color = neu.textPrimary)
                                Text(
                                    "${preset.subtitle} • Recommended profile",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.5.sp),
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
                    .padding(top = 12.dp),
                cornerRadius = 18.dp,
                contentPadding = PaddingValues(14.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Advanced Encoder Controls",
                        style = MaterialTheme.typography.titleMedium,
                        color = neu.textPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
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
            Spacer(modifier = Modifier.height(14.dp))
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 18.dp,
                contentPadding = PaddingValues(14.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(processingStatus, style = MaterialTheme.typography.labelLarge, color = neu.textPrimary)
                        Text("$processingProgress%", style = MaterialTheme.typography.labelLarge, color = neu.accentPrimary)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { processingProgress / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = neu.accentPrimary,
                        trackColor = neu.surfaceInset
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // 8. Bottom Primary Emerald CTA Button: "[↑] Export Video"
        EmeraldPillButton(
            text = "Export Video",
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

        Spacer(modifier = Modifier.height(14.dp))
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
 * Screen 5 — Processing Complete! (Exact reference mockup implementation).
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
        "${abs(savedPct)}% HQ size increase"
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
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 1. Concentric Mint Halo Checkmark Hero + Title & Subtitle
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ProcessingCompleteCheckmarkHero()
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Processing Complete!",
                    color = neu.textPrimary,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 23.sp
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Your video is ready",
                    color = neu.textSecondary,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.5.sp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. Output Summary Card ("Nature_Video.mp4" / "24.5 MB → 18.7 MB" / "🌿 24% smaller")
            NeumorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("processing_complete_summary_card"),
                cornerRadius = 22.dp,
                contentPadding = PaddingValues(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 90.dp, height = 68.dp)
                            .clip(RoundedCornerShape(14.dp))
                    ) {
                        VideoThumbnailPreview(
                            thumbnailBitmap = thumbnailBitmap,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = fileName,
                            color = neu.textPrimary,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${EliteMatrixViewModel.formatBytes(origBytes)} → ${EliteMatrixViewModel.formatBytes(outBytes)}",
                            color = neu.textSecondary,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Eco,
                                contentDescription = null,
                                tint = neu.accentPrimary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = savingsLabel,
                                color = neu.accentPrimary,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // 3. Action Buttons ("▶ Open Video", "<o> Share", "[Folder] Go to Projects")
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
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

            Spacer(modifier = Modifier.height(16.dp))
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
            }
        },
        update = { view ->
            view.player = exoPlayer
        },
        modifier = modifier
    )
}

@Composable
fun VideoPlaybackModalDialog(
    uriString: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close Player")
            }
        },
        title = { Text("Video Player") },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black)
            ) {
                InlineExoPlayerView(
                    uriString = uriString,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    )
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
