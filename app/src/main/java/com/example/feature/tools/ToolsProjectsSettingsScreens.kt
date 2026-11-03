package com.example.feature.tools

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.VideoFile
import androidx.compose.material.icons.outlined.ViewInAr
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AppPreferencesState
import com.example.domain.model.AppFontFamily
import com.example.domain.model.BuiltInPresets
import com.example.domain.model.CompressionMode
import com.example.domain.model.ExecutionRoute
import com.example.domain.model.ExportConfiguration
import com.example.domain.model.ExportItem
import com.example.domain.model.JobItem
import com.example.domain.model.PresetItem
import com.example.domain.model.ThemeMode
import com.example.domain.model.ThemePalette
import com.example.domain.model.VideoMetadata
import com.example.feature.editor.SavePresetDialog
import com.example.feature.home.RecentProjectNeumorphicCard
import com.example.ui.theme.EmeraldPillButton
import com.example.ui.theme.GoogleLogoIcon
import com.example.ui.theme.LocalNeumorphicColors
import com.example.ui.theme.NeumorphicCard
import com.example.ui.theme.NeumorphicChoicePill
import com.example.ui.theme.NeumorphicPillButton
import com.example.ui.theme.ProcessingPerformanceModeSelector
import com.example.ui.theme.paletteFor
import com.example.ui.theme.rememberResponsiveMetrics
import com.example.viewmodel.ActiveToolModal
import com.example.viewmodel.EliteMatrixViewModel
import com.example.viewmodel.PrimaryScreen

@Composable
fun ToolsHubScreen(
    selectedVideo: VideoMetadata?,
    exportConfig: ExportConfiguration,
    presets: List<PresetItem>,
    activeModal: ActiveToolModal,
    onOpenModal: (ActiveToolModal) -> Unit,
    onNavigateScreen: (PrimaryScreen) -> Unit,
    onUpdateExportConfig: ((ExportConfiguration) -> ExportConfiguration) -> Unit,
    onApplyPreset: (PresetItem) -> Unit,
    onSaveNewPreset: (String) -> Unit,
    onDeletePreset: (PresetItem) -> Unit,
    onStartJob: (String) -> Unit,
    onImportVideo: () -> Unit,
    onLoadSampleVideo: () -> Unit,
    onBackToHome: () -> Unit
) {
    BackHandler {
        if (activeModal != ActiveToolModal.None) {
            onOpenModal(ActiveToolModal.None)
        } else {
            onBackToHome()
        }
    }

    val neu = LocalNeumorphicColors.current
    val metrics = rememberResponsiveMetrics()
    var showCreatePresetDialog by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = metrics.horizontalPadding, vertical = metrics.verticalPadding)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (activeModal != ActiveToolModal.None) {
                IconButton(onClick = { onOpenModal(ActiveToolModal.None) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Tools",
                        tint = neu.textPrimary
                    )
                }
            }
            Text(
                text = when (activeModal) {
                    ActiveToolModal.VideoInspector -> "Video Inspector (FFprobe)"
                    ActiveToolModal.PresetManager -> "Presets & HQ Profiles"
                    ActiveToolModal.FormatConverter -> "Format & Codec Converter"
                    ActiveToolModal.BitrateCalculator -> "Bitrate & Size Calculator"
                    ActiveToolModal.CompressWorkflow -> "HQ Video Compression"
                    ActiveToolModal.HqEncoderWorkflow -> "HQ Encoder"
                    ActiveToolModal.None -> "Pro Video Tools"
                },
                color = neu.textPrimary,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (activeModal) {
            ActiveToolModal.None -> {
                val tools = listOf(
                    Triple("Enhance Video", "Color profiles, aspect ratio, rotation, sharpening & denoise", Icons.Outlined.AutoFixHigh) to {
                        onNavigateScreen(PrimaryScreen.EnhanceVideo)
                    },
                    Triple("Compress Video", "LITE/ELITE/MAX modes, custom FPS, resolution & Smart HQ compression", Icons.Outlined.Layers) to {
                        onOpenModal(ActiveToolModal.CompressWorkflow)
                    },
                    Triple("HQ Encoder", "Editable HQ presets for TikTok, Reels, Shorts, 120FPS & Archive", Icons.Outlined.ViewInAr) to {
                        onOpenModal(ActiveToolModal.HqEncoderWorkflow)
                    },
                    Triple("Custom Export", "Manual resolution, 24–120 FPS CFR, codec, audio & anti-recompression", Icons.Outlined.Tune) to {
                        onNavigateScreen(PrimaryScreen.ExportSettings)
                    },
                    Triple("Video Inspector", "Inspect verified sample FPS, orientation, bitrate, resolution & HDR tags", Icons.Outlined.Info) to {
                        onOpenModal(ActiveToolModal.VideoInspector)
                    },
                    Triple("Preset Manager", "Create, apply, duplicate & sync custom presets", Icons.Outlined.BookmarkBorder) to {
                        onOpenModal(ActiveToolModal.PresetManager)
                    },
                    Triple("Format Converter", "Remux or transcode between MP4, MOV & MKV", Icons.Outlined.SwapHoriz) to {
                        onOpenModal(ActiveToolModal.FormatConverter)
                    },
                    Triple("Bitrate Calculator", "Calculate exact bitrate for target MB & duration", Icons.Outlined.Calculate) to {
                        onOpenModal(ActiveToolModal.BitrateCalculator)
                    }
                )

                tools.forEach { (info, action) ->
                    NeumorphicCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        cornerRadius = 18.dp,
                        contentPadding = PaddingValues(13.dp),
                        onClick = action
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(neu.accentPrimary.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = info.third,
                                    contentDescription = info.first,
                                    tint = neu.accentPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = info.first,
                                    color = neu.textPrimary,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = info.second,
                                    color = neu.textSecondary,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.5.sp)
                                )
                            }
                        }
                    }
                }
            }

            ActiveToolModal.VideoInspector -> {
                NeumorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        if (selectedVideo == null) {
                            Text(
                                text = "No video loaded yet. Import a video or generate the sample H.264 clip to inspect its stream metadata.",
                                color = neu.textSecondary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                NeumorphicPillButton(
                                    text = "Import Video",
                                    modifier = Modifier.weight(1f),
                                    onClick = onImportVideo
                                )
                                NeumorphicPillButton(
                                    text = "Load Sample",
                                    modifier = Modifier.weight(1f),
                                    onClick = onLoadSampleVideo
                                )
                            }
                        } else {
                            val rows = listOf(
                                "File Name" to selectedVideo.fileName,
                                "Container Format" to selectedVideo.containerFormat,
                                "File Size" to EliteMatrixViewModel.formatBytes(selectedVideo.sizeBytes),
                                "Duration" to "${"%.2f".format(selectedVideo.durationMs / 1000.0)} sec",
                                "Display Dimensions" to "${selectedVideo.exactDimensionsLabel} (${selectedVideo.resolutionLabel})",
                                "Orientation & Ratio" to "${selectedVideo.orientationCategoryLabel} (${selectedVideo.aspectRatioLabel})",
                                "Rotation Metadata" to "${selectedVideo.normalizedRotation}° (Coded ${selectedVideo.width}×${selectedVideo.height})",
                                "Verified Frame Rate" to selectedVideo.formattedFpsLabel,
                                "Video Bitrate" to "${selectedVideo.bitrateKbps} kbps (${"%.1f".format(selectedVideo.bitrateKbps / 1000f)} Mbps)",
                                "Video Codec" to selectedVideo.videoCodec,
                                "Audio Codec" to selectedVideo.audioCodec,
                                "Audio Spec" to "${selectedVideo.audioBitrateKbps} kbps • ${selectedVideo.audioSampleRateHz / 1000.0} kHz • ${selectedVideo.audioChannels}ch",
                                "Pixel Format" to selectedVideo.pixelFormat,
                                "Color Space / HDR" to selectedVideo.colorStandard,
                                "Streams" to "${selectedVideo.streamCount} track(s)"
                            )
                            rows.forEachIndexed { idx, (k, v) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 5.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(k, color = neu.textSecondary, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp))
                                    Text(v, color = neu.textPrimary, style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp))
                                }
                                if (idx < rows.lastIndex) {
                                    HorizontalDivider(color = neu.divider.copy(alpha = 0.6f))
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            NeumorphicPillButton(
                                text = "Inspect Another Video",
                                onClick = onImportVideo
                            )
                        }
                    }
                }
            }

            ActiveToolModal.PresetManager -> {
                EmeraldPillButton(
                    text = "+ Save Current Settings as Preset",
                    onClick = { showCreatePresetDialog = true }
                )
                Spacer(modifier = Modifier.height(12.dp))
                presets.forEach { preset ->
                    NeumorphicCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        cornerRadius = 18.dp,
                        contentPadding = PaddingValues(12.dp),
                        onClick = { onApplyPreset(preset) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = preset.name,
                                    color = neu.textPrimary,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "${preset.resolution} • ${preset.fps} FPS • ${preset.codec} • ${"%.1f".format(preset.bitrateKbps / 1000f)} Mbps",
                                    color = neu.textSecondary,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp)
                                )
                            }
                            if (!preset.isBuiltIn) {
                                IconButton(onClick = { onDeletePreset(preset) }) {
                                    Icon(
                                        imageVector = Icons.Outlined.DeleteOutline,
                                        contentDescription = "Delete Preset",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }

            ActiveToolModal.CompressWorkflow -> {
                val (targetW, targetH) = exportConfig.targetDimensionsPx(
                    sourceWidth = selectedVideo?.displayWidth ?: 1920,
                    sourceHeight = selectedVideo?.displayHeight ?: 1080
                )
                val compressBitrateKbps = exportConfig.effectiveBitrateKbps(isCompressionWorkflow = true)

                NeumorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "1. Processing Performance Mode",
                            color = neu.textPrimary,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ProcessingPerformanceModeSelector(
                            selectedMode = exportConfig.performanceMode,
                            onSelectMode = { mode ->
                                onUpdateExportConfig { it.copy(performanceMode = mode) }
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "2. Compression Profile",
                            color = neu.textPrimary,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        CompressionMode.entries.forEach { mode ->
                            val selected = exportConfig.compressionMode == mode && exportConfig.bitrateMode == "Auto"
                            NeumorphicCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                cornerRadius = 15.dp,
                                contentPadding = PaddingValues(11.dp),
                                onClick = {
                                    onUpdateExportConfig {
                                        it.copy(
                                            compressionMode = mode,
                                            crf = mode.defaultCrf,
                                            bitrateMode = "Auto"
                                        )
                                    }
                                }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = mode.displayName,
                                            color = if (selected) neu.accentPrimary else neu.textPrimary,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = mode.description,
                                            color = neu.textSecondary,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.5.sp)
                                        )
                                    }
                                    Text(
                                        text = "CRF ${mode.defaultCrf}",
                                        color = neu.accentPrimary,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider(color = neu.divider)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Custom Resolution in HQ Compression Mode
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "3. Target Resolution (Preserves Aspect Ratio)",
                                color = neu.textPrimary,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "${targetW}×${targetH}",
                                color = neu.accentPrimary,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("720p", "1080p", "2K", "4K").forEach { res ->
                                NeumorphicChoicePill(
                                    label = res,
                                    selected = exportConfig.resolution == res,
                                    onClick = { onUpdateExportConfig { it.copy(resolution = res) } }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Custom Frame Rate (24, 30, 50, 60, 90, 120 FPS) in HQ Compression Mode
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "4. Target Frame Rate (CFR)",
                                color = neu.textPrimary,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "${exportConfig.fps} FPS",
                                color = neu.accentPrimary,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(24, 30, 60).forEach { fpsVal ->
                                NeumorphicChoicePill(
                                    label = "$fpsVal FPS",
                                    selected = exportConfig.fps == fpsVal,
                                    onClick = { onUpdateExportConfig { it.copy(fps = fpsVal) } }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(50, 90, 120).forEach { fpsVal ->
                                NeumorphicChoicePill(
                                    label = "$fpsVal FPS",
                                    selected = exportConfig.fps == fpsVal,
                                    onClick = { onUpdateExportConfig { it.copy(fps = fpsVal) } }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Video Codec & Custom Bitrate in HQ Compression Mode
                        Text(
                            text = "5. Codec & Bitrate Control",
                            color = neu.textPrimary,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("H.264", "H.265", "AV1", "ProRes").forEach { codecName ->
                                NeumorphicChoicePill(
                                    label = codecName,
                                    selected = exportConfig.codec == codecName,
                                    onClick = { onUpdateExportConfig { it.copy(codec = codecName) } }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Bitrate: ${"%.1f".format(compressBitrateKbps / 1000f)} Mbps (${exportConfig.bitrateMode} • ${exportConfig.performanceMode.badgeLabel})",
                                color = neu.textPrimary,
                                style = MaterialTheme.typography.labelLarge
                            )
                            Text(
                                text = if (exportConfig.bitrateMode == "Custom") "Reset to Auto" else "Custom",
                                color = neu.accentPrimary,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.clickable {
                                    onUpdateExportConfig {
                                        it.copy(bitrateMode = if (it.bitrateMode == "Custom") "Auto" else "Custom")
                                    }
                                }
                            )
                        }
                        Slider(
                            value = compressBitrateKbps.toFloat().coerceIn(800f, 50000f),
                            onValueChange = { v ->
                                onUpdateExportConfig {
                                    it.copy(bitrateMode = "Custom", customBitrateKbps = v.toInt())
                                }
                            },
                            valueRange = 800f..50000f,
                            colors = SliderDefaults.colors(activeTrackColor = neu.accentPrimary)
                        )

                        val durationSec = ((selectedVideo?.durationMs ?: 15_000L) / 1000.0).coerceAtLeast(1.0)
                        val estOut = ((compressBitrateKbps + exportConfig.audioBitrateKbps) * 1000L / 8.0 * durationSec * 1.02).toLong()
                        Text(
                            text = "Estimated Output: ~${EliteMatrixViewModel.formatBytes(estOut)} at ${targetW}×${targetH} • ${exportConfig.fps} FPS",
                            color = neu.textSecondary,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.5.sp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        EmeraldPillButton(
                            text = "Start HQ Compression (${exportConfig.performanceMode.badgeLabel} • ${exportConfig.fps} FPS)",
                            onClick = { onStartJob("compress") }
                        )
                    }
                }
            }

            ActiveToolModal.HqEncoderWorkflow -> {
                NeumorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Processing Performance Mode",
                            color = neu.textPrimary,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        ProcessingPerformanceModeSelector(
                            selectedMode = exportConfig.performanceMode,
                            onSelectMode = { mode ->
                                onUpdateExportConfig { it.copy(performanceMode = mode) }
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "HQ Encoder Starting Profiles",
                            color = neu.textPrimary,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Editable quality-focused encoding configurations (locks exact CFR frame rate, aspect ratio, and BT.709 color accuracy).",
                            color = neu.textSecondary,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.5.sp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        BuiltInPresets.defaultHqPresets.forEach { preset ->
                            val isSelected = exportConfig.hqEncoderPresetName == preset.name
                            NeumorphicCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                cornerRadius = 15.dp,
                                contentPadding = PaddingValues(11.dp),
                                onClick = { onApplyPreset(preset) }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = preset.name,
                                            color = if (isSelected) neu.accentPrimary else neu.textPrimary,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "${preset.resolution} • ${preset.fps} FPS • ${preset.codec} • ${"%.1f".format(preset.bitrateKbps / 1000f)} Mbps",
                                            color = neu.textSecondary,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.5.sp)
                                        )
                                    }
                                    Text(
                                        text = if (isSelected) "Active" else "Select",
                                        color = neu.accentPrimary,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        EmeraldPillButton(
                            text = "Run HQ Encode (${exportConfig.performanceMode.badgeLabel} • ${exportConfig.resolution} • ${exportConfig.fps} FPS)",
                            onClick = { onStartJob("hq_encode") }
                        )
                    }
                }
            }

            ActiveToolModal.FormatConverter -> {
                NeumorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Target Container Format", style = MaterialTheme.typography.titleMedium, color = neu.textPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            listOf("MP4", "MOV", "MKV", "WebM").forEach { fmt ->
                                NeumorphicChoicePill(
                                    label = fmt,
                                    selected = exportConfig.containerFormat == fmt,
                                    onClick = { onUpdateExportConfig { it.copy(containerFormat = fmt) } }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("Target Video Stream Codec", style = MaterialTheme.typography.titleMedium, color = neu.textPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            listOf("H.264", "H.265", "AV1", "ProRes").forEach { c ->
                                NeumorphicChoicePill(
                                    label = c,
                                    selected = exportConfig.codec == c,
                                    onClick = { onUpdateExportConfig { it.copy(codec = c) } }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        EmeraldPillButton(
                            text = "Convert to ${exportConfig.containerFormat} (${exportConfig.codec})",
                            onClick = { onStartJob("convert") }
                        )
                    }
                }
            }

            ActiveToolModal.BitrateCalculator -> {
                var durationSeconds by rememberSaveable {
                    mutableFloatStateOf(((selectedVideo?.durationMs ?: 30_000L) / 1000f).coerceIn(5f, 600f))
                }
                var targetSizeMb by rememberSaveable { mutableFloatStateOf(20f) }
                val calculatedVideoKbps = remember(durationSeconds, targetSizeMb) {
                    val totalBits = targetSizeMb * 1024f * 1024f * 8f * 0.96f // 4% container overhead
                    val totalKbps = (totalBits / durationSeconds) / 1000f
                    (totalKbps - 192f).toInt().coerceIn(500, 100_000)
                }

                NeumorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Clip Duration: ${durationSeconds.toInt()} sec",
                            color = neu.textPrimary,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Slider(
                            value = durationSeconds,
                            onValueChange = { durationSeconds = it },
                            valueRange = 5f..600f,
                            colors = SliderDefaults.colors(activeTrackColor = neu.accentPrimary)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Target File Size: ${"%.1f".format(targetSizeMb)} MB",
                            color = neu.textPrimary,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Slider(
                            value = targetSizeMb,
                            onValueChange = { targetSizeMb = it },
                            valueRange = 2f..250f,
                            colors = SliderDefaults.colors(activeTrackColor = neu.accentPrimary)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Recommended Video Bitrate: $calculatedVideoKbps kbps (${"%.1f".format(calculatedVideoKbps / 1000f)} Mbps) + ${exportConfig.audioBitrateKbps} kbps Audio",
                            color = neu.accentPrimary,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        EmeraldPillButton(
                            text = "Apply $calculatedVideoKbps kbps to Export Settings",
                            onClick = {
                                onUpdateExportConfig {
                                    it.copy(
                                        bitrateMode = "Custom",
                                        customBitrateKbps = calculatedVideoKbps
                                    )
                                }
                                onNavigateScreen(PrimaryScreen.ExportSettings)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showCreatePresetDialog) {
        SavePresetDialog(
            onDismiss = { showCreatePresetDialog = false },
            onConfirmSave = { name ->
                showCreatePresetDialog = false
                onSaveNewPreset(name)
            }
        )
    }
}

/**
 * Redesigned Projects & Queue Screen:
 * Eliminates wasted whitespace, displays compact organized cards for both
 * Active/Queued/Historical Jobs (with Pause, Resume, Cancel, Retry, Remove)
 * and Completed Exports (with Play, Share, Remove).
 */
@Composable
fun ProjectsHistoryScreen(
    jobs: List<JobItem>,
    exports: List<ExportItem>,
    thumbnailBitmap: Bitmap?,
    isProcessing: Boolean = false,
    isPaused: Boolean = false,
    activeJobId: String? = null,
    elapsedTimeMs: Long = 0L,
    estimatedRemainingMs: Long = 0L,
    onPauseActiveJob: () -> Unit = {},
    onResumeActiveJob: () -> Unit = {},
    onCancelActiveJob: () -> Unit = {},
    onRetryJob: (JobItem) -> Unit = {},
    onRemoveJob: (JobItem) -> Unit = {},
    onOpenExport: (ExportItem) -> Unit,
    onShareExport: (ExportItem) -> Unit,
    onDeleteExport: (ExportItem) -> Unit,
    onImportNewVideo: () -> Unit,
    onBackToHome: () -> Unit
) {
    BackHandler(onBack = onBackToHome)
    val neu = LocalNeumorphicColors.current
    val metrics = rememberResponsiveMetrics()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = metrics.horizontalPadding, vertical = metrics.verticalPadding)
    ) {
        // Compact Header Row with Summary Badges & "+ Import" Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Projects & Queue",
                    color = neu.textPrimary,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                )
                Text(
                    text = "${exports.size} Completed • ${jobs.size} Queue Job(s)",
                    color = neu.textSecondary,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp)
                )
            }

            NeumorphicPillButton(
                text = "+ Import Video",
                height = 36.dp,
                testTag = "projects_import_video_button",
                modifier = Modifier.width(132.dp),
                onClick = onImportNewVideo
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (exports.isEmpty() && jobs.isEmpty()) {
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp,
                contentPadding = PaddingValues(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.VideoFile,
                        contentDescription = null,
                        tint = neu.accentPrimary,
                        modifier = Modifier.size(34.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No Active Jobs or Exports Yet",
                        color = neu.textPrimary,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Import a video and run Enhance, Compress, or Export to populate your queue and gallery.",
                        color = neu.textSecondary,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    EmeraldPillButton(
                        text = "Import Video to Start",
                        onClick = onImportNewVideo
                    )
                }
            }
        } else {
            // 1. Processing Queue & Job History Section (Compact Cards with Pause/Resume/Cancel/Retry/Remove)
            if (jobs.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Processing Queue (${jobs.size})",
                        color = neu.textPrimary,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.5.sp
                        )
                    )
                    if (isProcessing) {
                        Text(
                            text = if (isPaused) "PAUSED" else "RUNNING LIVE",
                            color = if (isPaused) neu.sunAmber else neu.accentPrimary,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                jobs.take(12).forEach { job ->
                    val isThisJobActive = isProcessing && (job.id == activeJobId || job.status in listOf("processing", "paused", "queued"))
                    val statusUpper = job.status.uppercase()
                    val statusColor = when (job.status.lowercase()) {
                        "completed" -> neu.accentPrimary
                        "paused" -> neu.sunAmber
                        "failed", "cancelled" -> MaterialTheme.colorScheme.error
                        else -> neu.accentSecondary
                    }

                    NeumorphicCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .testTag("queue_job_card_${job.id}"),
                        cornerRadius = 16.dp,
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Top row: File name + Operation badge + Status badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = job.sourceFileName,
                                        color = neu.textPrimary,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(neu.accentPrimary.copy(alpha = 0.14f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = job.operationType.uppercase(),
                                            color = neu.accentPrimary,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                }

                                Text(
                                    text = "$statusUpper (${job.progressPercent}%)",
                                    color = statusColor,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Spec Summary Row (Performance mode, resolution, FPS, codec, route)
                            Text(
                                text = "${job.configSummary} • Route: ${job.executionRoute.uppercase()}",
                                color = neu.textSecondary,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.5.sp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (!job.errorMessage.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = job.errorMessage,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                                    maxLines = 2
                                )
                            }

                            // Progress Bar + Elapsed/ETA for Active or In-Progress Jobs
                            if (isThisJobActive || job.progressPercent in 1..99) {
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { (job.progressPercent / 100f).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(5.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = statusColor,
                                    trackColor = neu.surfaceInset
                                )
                                if (isThisJobActive) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Elapsed: ${EliteMatrixViewModel.formatDurationSeconds(elapsedTimeMs)}",
                                            color = neu.textSecondary,
                                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.5.sp)
                                        )
                                        Text(
                                            text = "ETA: ${if (estimatedRemainingMs > 0L) EliteMatrixViewModel.formatDurationSeconds(estimatedRemainingMs) else "Estimating..."}",
                                            color = neu.accentPrimary,
                                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.5.sp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Compact Action Buttons Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (isThisJobActive) {
                                    NeumorphicPillButton(
                                        text = if (isPaused) "Resume" else "Pause",
                                        height = 32.dp,
                                        testTag = "job_pause_resume_${job.id}",
                                        leadingContent = {
                                            Icon(
                                                imageVector = if (isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                                                contentDescription = null,
                                                tint = neu.accentPrimary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        },
                                        modifier = Modifier.weight(1f),
                                        onClick = { if (isPaused) onResumeActiveJob() else onPauseActiveJob() }
                                    )
                                    NeumorphicPillButton(
                                        text = "Cancel",
                                        height = 32.dp,
                                        testTag = "job_cancel_${job.id}",
                                        leadingContent = {
                                            Icon(
                                                imageVector = Icons.Filled.Close,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        },
                                        modifier = Modifier.weight(1f),
                                        onClick = onCancelActiveJob
                                    )
                                } else {
                                    NeumorphicPillButton(
                                        text = if (job.status in listOf("failed", "cancelled")) "Retry Job" else "Re-Run",
                                        height = 32.dp,
                                        testTag = "job_retry_${job.id}",
                                        leadingContent = {
                                            Icon(
                                                imageVector = Icons.Outlined.Refresh,
                                                contentDescription = null,
                                                tint = neu.accentPrimary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        },
                                        modifier = Modifier.weight(1f),
                                        onClick = { onRetryJob(job) }
                                    )
                                    NeumorphicPillButton(
                                        text = "Remove",
                                        height = 32.dp,
                                        testTag = "job_remove_${job.id}",
                                        leadingContent = {
                                            Icon(
                                                imageVector = Icons.Outlined.DeleteOutline,
                                                contentDescription = null,
                                                tint = neu.textSecondary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        },
                                        modifier = Modifier.weight(1f),
                                        onClick = { onRemoveJob(job) }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // 2. Completed Exports Section
            if (exports.isNotEmpty()) {
                Text(
                    text = "Completed Exports (${exports.size})",
                    color = neu.textPrimary,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                exports.forEach { item ->
                    RecentProjectNeumorphicCard(
                        exportItem = item,
                        thumbnailBitmap = thumbnailBitmap,
                        onOpenClick = { onOpenExport(item) },
                        onShareClick = { onShareExport(item) },
                        onDeleteClick = { onDeleteExport(item) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    userEmail: String?,
    userName: String?,
    isGuestMode: Boolean,
    preferences: AppPreferencesState,
    hasNotificationPermission: Boolean = true,
    hasGalleryStorageAccess: Boolean = true,
    onRequestAllPermissions: () -> Unit = {},
    onOpenFullFilePicker: () -> Unit = {},
    onSelectPalette: (ThemePalette) -> Unit,
    onSelectThemeMode: (ThemeMode) -> Unit,
    onSelectFontFamily: (AppFontFamily) -> Unit = {},
    onSelectRoute: (ExecutionRoute) -> Unit,
    onTogglePreserveMetadata: (Boolean) -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onSignOutOrSignInWithGoogle: () -> Unit,
    onBackToHome: () -> Unit
) {
    BackHandler(onBack = onBackToHome)
    val neu = LocalNeumorphicColors.current
    val metrics = rememberResponsiveMetrics()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = metrics.horizontalPadding, vertical = metrics.verticalPadding)
    ) {
        Text(
            text = "Settings & Appearance",
            color = neu.textPrimary,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 1. Account & Cloud Sync Card
        NeumorphicCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 20.dp,
            contentPadding = PaddingValues(14.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (isGuestMode) "Guest Mode (Local-Only)" else (userName ?: "Authenticated Creator"),
                    color = neu.textPrimary,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = if (isGuestMode) {
                        "Your presets and exports are stored locally on this device. Sign in with Google to enable Firestore Cloud Sync & Cloud Run FFmpeg workers."
                    } else {
                        "Signed in as ${userEmail ?: "Google Account"} • Cloud Sync Active"
                    },
                    color = neu.textSecondary,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                NeumorphicPillButton(
                    text = if (isGuestMode) "Sign in with Google to Sync" else "Sign Out",
                    height = 42.dp,
                    testTag = "settings_auth_action_button",
                    leadingContent = if (isGuestMode) {
                        { GoogleLogoIcon(Modifier.size(18.dp)) }
                    } else null,
                    onClick = onSignOutOrSignInWithGoogle
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Permissions, Gallery Auto-Save & Full Files Access Card
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.Security,
                contentDescription = null,
                tint = neu.accentPrimary,
                modifier = Modifier.size(19.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Permissions, Gallery & Files Access",
                color = neu.textPrimary,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        NeumorphicCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 20.dp,
            contentPadding = PaddingValues(14.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Notifications Permission",
                            style = MaterialTheme.typography.titleMedium,
                            color = neu.textPrimary
                        )
                        Text(
                            text = "Real-time completion alerts when video exports finish",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.5.sp),
                            color = neu.textSecondary
                        )
                    }
                    Text(
                        text = if (hasNotificationPermission) "Granted" else "Tap Below",
                        color = if (hasNotificationPermission) neu.accentPrimary else neu.thermoOrange,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = neu.divider)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Gallery Auto-Save & Media Storage",
                            style = MaterialTheme.typography.titleMedium,
                            color = neu.textPrimary
                        )
                        Text(
                            text = "Automatically saves processed videos to Movies/EliteMatrix in Gallery",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.5.sp),
                            color = neu.textSecondary
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = if (hasGalleryStorageAccess) neu.accentPrimary else neu.thermoOrange,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (hasGalleryStorageAccess) "Active" else "Request",
                            color = if (hasGalleryStorageAccess) neu.accentPrimary else neu.thermoOrange,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                EmeraldPillButton(
                    text = "Grant All Permissions (Notifications & Gallery)",
                    testTag = "grant_all_permissions_button",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.NotificationsActive,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    onClick = onRequestAllPermissions
                )

                Spacer(modifier = Modifier.height(8.dp))

                NeumorphicPillButton(
                    text = "Browse All Video Files (Full Storage Picker)",
                    height = 42.dp,
                    testTag = "open_full_file_picker_button",
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Outlined.FolderOpen,
                            contentDescription = null,
                            tint = neu.accentPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    onClick = onOpenFullFilePicker
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Theme Mode Selector (Light, Dark, Follow System)
        Text(
            text = "Appearance Mode",
            color = neu.textPrimary,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ThemeMode.entries.forEach { mode ->
                NeumorphicChoicePill(
                    label = mode.displayName,
                    selected = preferences.themeMode == mode,
                    onClick = { onSelectThemeMode(mode) }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Typography & Font Styles Selector
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.TextFields,
                contentDescription = null,
                tint = neu.accentPrimary,
                modifier = Modifier.size(19.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Typography & Font Style",
                color = neu.textPrimary,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppFontFamily.entries.forEach { fontOption ->
                val isSelected = preferences.fontFamily == fontOption
                NeumorphicCard(
                    cornerRadius = 15.dp,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    onClick = { onSelectFontFamily(fontOption) }
                ) {
                    Column {
                        Text(
                            text = fontOption.displayName,
                            color = if (isSelected) neu.accentPrimary else neu.textPrimary,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        )
                        Text(
                            text = fontOption.subtitle,
                            color = neu.textSecondary,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 10.sp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 5. Neumorphic Color Palettes (16 Custom Light & Dark Themes)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.Palette,
                contentDescription = null,
                tint = neu.accentPrimary,
                modifier = Modifier.size(19.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Neumorphic Color Themes (${ThemePalette.entries.size})",
                color = neu.textPrimary,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ThemePalette.entries.forEach { palette ->
                val isSelected = preferences.themePalette == palette
                val previewColors = paletteFor(palette, palette.isDarkPalette)
                NeumorphicCard(
                    cornerRadius = 15.dp,
                    contentPadding = PaddingValues(horizontal = 11.dp, vertical = 8.dp),
                    onClick = { onSelectPalette(palette) }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(previewColors.canvas)
                                .border(2.dp, previewColors.accentPrimary, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(7.dp))
                        Text(
                            text = palette.displayName,
                            color = if (isSelected) neu.accentPrimary else neu.textPrimary,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 6. Local / Cloud Routing & Privacy
        Text(
            text = "Processing Route (Local / Cloud Run)",
            color = neu.textPrimary,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ExecutionRoute.entries.forEach { route ->
                NeumorphicChoicePill(
                    label = route.label,
                    selected = preferences.defaultRoute == route,
                    onClick = { onSelectRoute(route) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        NeumorphicCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 18.dp,
            contentPadding = PaddingValues(14.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Preserve Video Metadata", style = MaterialTheme.typography.titleMedium, color = neu.textPrimary)
                        Text("Retain compatible color tags and creation timestamps", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.5.sp), color = neu.textSecondary)
                    }
                    Switch(
                        checked = preferences.preserveMetadata,
                        onCheckedChange = onTogglePreserveMetadata
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = neu.divider)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Job Completion Alerts", style = MaterialTheme.typography.titleMedium, color = neu.textPrimary)
                        Text("Notify when background or cloud exports finish", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.5.sp), color = neu.textSecondary)
                    }
                    Switch(
                        checked = preferences.notificationsEnabled,
                        onCheckedChange = onToggleNotifications
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 7. Developer Credit Card ("Made with ❤️ by Rahul Shah")
        NeumorphicCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("developer_credit_card"),
            cornerRadius = 20.dp,
            contentPadding = PaddingValues(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Made with ",
                        color = neu.textPrimary,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    )
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "Love",
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(17.dp)
                    )
                    Text(
                        text = " by Rahul Shah",
                        color = neu.accentPrimary,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "ELITE MATRIX • HQ Video Enhancer & Real-Time Media Studio",
                    color = neu.textSecondary,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.5.sp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}
