package com.example.feature.home

import android.graphics.Bitmap
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.VideoFile
import androidx.compose.material.icons.outlined.ViewInAr
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.domain.model.ExportItem
import com.example.domain.model.JobItem
import com.example.domain.model.VideoMetadata
import com.example.ui.theme.LocalNeumorphicColors
import com.example.ui.theme.NeumorphicCard
import com.example.ui.theme.NeumorphicPillButton
import com.example.ui.theme.VideoThumbnailPreview
import com.example.ui.theme.neumorphicRaised
import com.example.ui.theme.rememberResponsiveMetrics
import com.example.viewmodel.ActiveToolModal
import com.example.viewmodel.EliteMatrixViewModel
import com.example.viewmodel.PrimaryScreen

@Composable
fun HomeDashboardScreen(
    userName: String?,
    userPhotoUrl: String?,
    isGuestMode: Boolean,
    selectedVideo: VideoMetadata?,
    videoThumbnail: Bitmap?,
    recentExports: List<ExportItem>,
    activeJobs: List<JobItem>,
    isProcessing: Boolean,
    isPaused: Boolean = false,
    processingProgress: Int,
    processingStatus: String,
    elapsedTimeMs: Long = 0L,
    estimatedRemainingMs: Long = 0L,
    onPauseProcessing: () -> Unit = {},
    onResumeProcessing: () -> Unit = {},
    onCancelProcessing: () -> Unit = {},
    onImportVideoClick: () -> Unit,
    onLoadSampleVideoClick: () -> Unit,
    onNavigatePrimary: (PrimaryScreen) -> Unit,
    onOpenToolModal: (ActiveToolModal) -> Unit,
    onOpenExportedVideo: (ExportItem) -> Unit,
    onShareExportedVideo: (ExportItem) -> Unit,
    onDeleteExportedVideo: (ExportItem) -> Unit,
    onSignOutOrLinkAccount: () -> Unit
) {
    val neu = LocalNeumorphicColors.current
    val metrics = rememberResponsiveMetrics()
    var showHeaderMenu by remember { mutableStateOf(false) }
    var showNotificationsSheet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = metrics.horizontalPadding,
                vertical = metrics.verticalPadding
            )
    ) {
        // 1. Top Header Bar (Menu icon, Notification Bell with coral badge, Avatar)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                IconButton(
                    onClick = { showHeaderMenu = true },
                    modifier = Modifier.testTag("home_menu_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = neu.textPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                DropdownMenu(
                    expanded = showHeaderMenu,
                    onDismissRequest = { showHeaderMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Import Video File") },
                        onClick = {
                            showHeaderMenu = false
                            onImportVideoClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Load Sample Clip (Nature_Video.mp4)") },
                        onClick = {
                            showHeaderMenu = false
                            onLoadSampleVideoClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Video Inspector (FFprobe)") },
                        onClick = {
                            showHeaderMenu = false
                            onOpenToolModal(ActiveToolModal.VideoInspector)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (isGuestMode) "Sign in with Google" else "Sign Out") },
                        onClick = {
                            showHeaderMenu = false
                            onSignOutOrLinkAccount()
                        }
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Notification Bell with Coral Dot
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable { showNotificationsSheet = !showNotificationsSheet },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint = neu.textPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = (-8).dp, y = 8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE85445))
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // User Avatar
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                listOf(Color(0xFFD9C3B0), Color(0xFFB39278))
                            )
                        )
                        .border(1.5.dp, Color.White, CircleShape)
                        .clickable { onNavigatePrimary(PrimaryScreen.SettingsScreen) }
                        .testTag("home_avatar_button"),
                    contentAlignment = Alignment.Center
                ) {
                    if (!userPhotoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = userPhotoUrl,
                            contentDescription = "User Profile Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = "Creator Profile",
                            tint = Color(0xFF2B211B),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        if (showNotificationsSheet) {
            Spacer(modifier = Modifier.height(6.dp))
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 16.dp,
                contentPadding = PaddingValues(12.dp)
            ) {
                Column {
                    Text(
                        text = "Processing Queue & Status",
                        style = MaterialTheme.typography.titleMedium,
                        color = neu.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isProcessing) {
                            "Active Job: $processingStatus ($processingProgress%)"
                        } else if (recentExports.isNotEmpty()) {
                            "Last export completed: ${recentExports.first().fileName} (${EliteMatrixViewModel.formatBytes(recentExports.first().outputSizeBytes)})"
                        } else {
                            "All workers ready. Import a video or tap any tool tile to begin."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = neu.textSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 2. Greeting Header ("Hello, Creator!" + "Turn your videos into something more ✨")
        Text(
            text = if (!userName.isNullOrBlank() && !isGuestMode) {
                "Hello, ${userName.substringBefore(' ')}!"
            } else {
                "Hello, Creator!"
            },
            color = neu.textPrimary,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 23.sp
            )
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Turn your videos into something more ✨",
            color = neu.textSecondary,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
        )

        Spacer(modifier = Modifier.height(metrics.sectionSpacing))

        // 3. Hero Banner Card ("Premium Video Tools" / "Enhance. Compress. Export." + 3D Mint Play Prism + Arrow button)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(metrics.heroBannerHeight)
                .testTag("premium_video_tools_banner")
                .drawBehind {
                    val r = 22.dp.toPx()
                    drawRoundRect(
                        color = neu.shadowDark.copy(alpha = 0.45f),
                        topLeft = Offset(3.dp.toPx(), 5.dp.toPx()),
                        size = Size(size.width, size.height),
                        cornerRadius = CornerRadius(r, r)
                    )
                }
                .clip(RoundedCornerShape(22.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            neu.accentSoftMintStart,
                            neu.accentSoftMintEnd
                        )
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.55f), RoundedCornerShape(22.dp))
                .clickable { onNavigatePrimary(PrimaryScreen.EnhanceVideo) }
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Text(
                    text = "Premium\nVideo Tools",
                    color = Color(0xFF132A20),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        lineHeight = 22.sp
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Enhance. Compress. Export.",
                    color = Color(0xFF2E5947),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp
                    )
                )
            }

            // 3D Mint Play Prism on Right
            Canvas(
                modifier = Modifier
                    .size(78.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = (-18).dp, y = (-4).dp)
            ) {
                val w = size.width
                val h = size.height
                val p = Path().apply {
                    moveTo(w * 0.22f, h * 0.14f)
                    lineTo(w * 0.88f, h * 0.50f)
                    lineTo(w * 0.22f, h * 0.86f)
                    close()
                }
                drawPath(
                    path = p,
                    color = Color(0xFF53B891).copy(alpha = 0.45f),
                    style = Stroke(width = 15.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                drawPath(
                    path = p,
                    brush = Brush.linearGradient(
                        listOf(Color(0xFFE7F9F0), Color(0xFF8CDABA))
                    ),
                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }

            // Small circular white raised button with right arrow at bottom-right
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(Color(0xFFF6F9F7))
                    .border(1.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Open Enhance Video",
                    tint = Color(0xFF174D38),
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        // Active Processing Queue Progress Banner (with Pause/Resume/Cancel and Elapsed/ETA)
        if (isProcessing) {
            Spacer(modifier = Modifier.height(metrics.compactSpacing))
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
                                color = neu.textPrimary,
                                style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Elapsed: ${EliteMatrixViewModel.formatDurationSeconds(elapsedTimeMs)} • ETA: ${if (estimatedRemainingMs > 0L) EliteMatrixViewModel.formatDurationSeconds(estimatedRemainingMs) else "Calculating..."}",
                                color = neu.textSecondary,
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.5.sp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$processingProgress%",
                            color = neu.accentPrimary,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
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
                            height = 34.dp,
                            leadingContent = {
                                Icon(
                                    imageVector = if (isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                                    contentDescription = null,
                                    tint = neu.accentPrimary,
                                    modifier = Modifier.size(15.dp)
                                )
                            },
                            modifier = Modifier.weight(1f),
                            onClick = { if (isPaused) onResumeProcessing() else onPauseProcessing() }
                        )
                        NeumorphicPillButton(
                            text = "Cancel",
                            height = 34.dp,
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(15.dp)
                                )
                            },
                            modifier = Modifier.weight(1f),
                            onClick = onCancelProcessing
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(metrics.sectionSpacing))

        // 4. Primary 2x2 Neumorphic Action Tiles Grid
        // Row 1: Enhance Video | Compress Video
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PrimaryActionNeumorphicTile(
                title = "Enhance\nVideo",
                icon = Icons.Outlined.AutoFixHigh,
                tileHeight = metrics.actionTileHeight,
                testTag = "tile_enhance_video",
                modifier = Modifier.weight(1f),
                onClick = { onNavigatePrimary(PrimaryScreen.EnhanceVideo) }
            )
            PrimaryActionNeumorphicTile(
                title = "Compress\nVideo",
                icon = Icons.Outlined.Layers,
                tileHeight = metrics.actionTileHeight,
                testTag = "tile_compress_video",
                modifier = Modifier.weight(1f),
                onClick = { onOpenToolModal(ActiveToolModal.CompressWorkflow) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Row 2: HQ Encoder | Custom Export
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PrimaryActionNeumorphicTile(
                title = "HQ\nEncoder",
                icon = Icons.Outlined.ViewInAr,
                tileHeight = metrics.actionTileHeight,
                testTag = "tile_hq_encoder",
                modifier = Modifier.weight(1f),
                onClick = { onOpenToolModal(ActiveToolModal.HqEncoderWorkflow) }
            )
            PrimaryActionNeumorphicTile(
                title = "Custom\nExport",
                icon = Icons.Outlined.Tune,
                tileHeight = metrics.actionTileHeight,
                testTag = "tile_custom_export",
                modifier = Modifier.weight(1f),
                onClick = { onNavigatePrimary(PrimaryScreen.ExportSettings) }
            )
        }

        Spacer(modifier = Modifier.height(metrics.sectionSpacing))

        // Quick Tools Strip (Info, Preset, Converter, Calculator) per PRD Section 5
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickToolMiniChip(
                label = "Info",
                icon = Icons.Outlined.Info,
                modifier = Modifier.weight(1f),
                onClick = { onOpenToolModal(ActiveToolModal.VideoInspector) }
            )
            QuickToolMiniChip(
                label = "Preset",
                icon = Icons.Outlined.BookmarkBorder,
                modifier = Modifier.weight(1f),
                onClick = { onOpenToolModal(ActiveToolModal.PresetManager) }
            )
            QuickToolMiniChip(
                label = "Converter",
                icon = Icons.Outlined.SwapHoriz,
                modifier = Modifier.weight(1f),
                onClick = { onOpenToolModal(ActiveToolModal.FormatConverter) }
            )
            QuickToolMiniChip(
                label = "Calculator",
                icon = Icons.Outlined.Calculate,
                modifier = Modifier.weight(1f),
                onClick = { onOpenToolModal(ActiveToolModal.BitrateCalculator) }
            )
        }

        Spacer(modifier = Modifier.height(metrics.sectionSpacing))

        // 5. Recent Projects Section Header ("Recent Projects" + "See All ->")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Projects",
                color = neu.textPrimary,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.5.sp
                )
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .testTag("recent_projects_see_all")
                    .clickable { onNavigatePrimary(PrimaryScreen.ProjectsHistory) }
                    .padding(vertical = 4.dp, horizontal = 4.dp)
            ) {
                Text(
                    text = "See All",
                    color = neu.accentSecondary,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.5.sp
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "See All Projects",
                    tint = neu.accentSecondary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (recentExports.isNotEmpty()) {
            recentExports.take(3).forEach { exportItem ->
                RecentProjectNeumorphicCard(
                    exportItem = exportItem,
                    thumbnailBitmap = videoThumbnail,
                    onOpenClick = { onOpenExportedVideo(exportItem) },
                    onShareClick = { onShareExportedVideo(exportItem) },
                    onDeleteClick = { onDeleteExportedVideo(exportItem) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        } else {
            // Compact Active Clip / Empty State Card
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 18.dp,
                contentPadding = PaddingValues(12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(68.dp, 50.dp)
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            VideoThumbnailPreview(
                                thumbnailBitmap = videoThumbnail,
                                preserveAspectRatio = true,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedVideo?.fileName ?: "No Recent Exports Yet",
                                color = neu.textPrimary,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (selectedVideo != null) {
                                    "${selectedVideo.exactDimensionsLabel} • ${selectedVideo.orientationCategoryLabel} • ${EliteMatrixViewModel.formatBytes(selectedVideo.sizeBytes)}"
                                } else {
                                    "Import a video from your device or load our real H.264 sample clip"
                                },
                                color = neu.textSecondary,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.5.sp),
                                maxLines = 2
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NeumorphicPillButton(
                            text = "Import Video",
                            height = 40.dp,
                            testTag = "home_import_video_button",
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Outlined.VideoFile,
                                    contentDescription = null,
                                    tint = neu.accentPrimary,
                                    modifier = Modifier.size(17.dp)
                                )
                            },
                            modifier = Modifier.weight(1f),
                            onClick = onImportVideoClick
                        )
                        NeumorphicPillButton(
                            text = "Sample Clip",
                            height = 40.dp,
                            testTag = "home_load_sample_button",
                            modifier = Modifier.weight(1f),
                            onClick = onLoadSampleVideoClick
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun PrimaryActionNeumorphicTile(
    title: String,
    icon: ImageVector,
    tileHeight: androidx.compose.ui.unit.Dp,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val neu = LocalNeumorphicColors.current
    Box(
        modifier = modifier
            .height(tileHeight)
            .testTag(testTag)
            .neumorphicRaised(cornerRadius = 20.dp, elevation = 5.dp)
            .clickable(onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title.replace('\n', ' '),
                tint = neu.accentPrimary,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.height(7.dp))
            Text(
                text = title,
                color = neu.textPrimary,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    lineHeight = 16.sp
                )
            )
        }
    }
}

@Composable
private fun QuickToolMiniChip(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val neu = LocalNeumorphicColors.current
    Box(
        modifier = modifier
            .height(38.dp)
            .neumorphicRaised(cornerRadius = 14.dp, elevation = 3.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = neu.accentPrimary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = neu.textPrimary,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                ),
                maxLines = 1
            )
        }
    }
}

/**
 * Compact Recent Project card matching Screen 2 reference mockup:
 * Preserves aspect ratio in thumbnail, shows filename, resolution, codec, format, size,
 * green CheckCircle + "Completed", 3-dot menu on top-right, relative timestamp on bottom-right.
 */
@Composable
fun RecentProjectNeumorphicCard(
    exportItem: ExportItem,
    thumbnailBitmap: Bitmap?,
    onOpenClick: () -> Unit,
    onShareClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val neu = LocalNeumorphicColors.current
    var menuExpanded by remember { mutableStateOf(false) }

    val relativeTime = remember(exportItem.createdAtMillis) {
        val diffMin = ((System.currentTimeMillis() - exportItem.createdAtMillis) / 60_000L).coerceAtLeast(0L)
        when {
            diffMin < 2L -> "Just now"
            diffMin < 60L -> "${diffMin}m ago"
            diffMin < 1440L -> "${diffMin / 60L}h ago"
            else -> "${diffMin / 1440L}d ago"
        }
    }

    NeumorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("recent_project_card_${exportItem.id}"),
        cornerRadius = 18.dp,
        contentPadding = PaddingValues(10.dp),
        onClick = onOpenClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left rounded thumbnail preserving exact video aspect ratio
            Box(
                modifier = Modifier
                    .size(width = 76.dp, height = 56.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                VideoThumbnailPreview(
                    thumbnailBitmap = thumbnailBitmap,
                    preserveAspectRatio = true,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Middle info column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exportItem.fileName,
                    color = neu.textPrimary,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${exportItem.resolution} • ${exportItem.codec} (${exportItem.containerFormat}) • ${EliteMatrixViewModel.formatBytes(exportItem.outputSizeBytes)}",
                    color = neu.textSecondary,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.5.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Completed",
                        tint = neu.accentPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Completed",
                        color = neu.accentPrimary,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.5.sp
                        )
                    )
                }
            }

            // Right column: 3-dot menu + timestamp
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.height(54.dp)
            ) {
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = neu.textSecondary,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Open Video") },
                            onClick = {
                                menuExpanded = false
                                onOpenClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Share") },
                            onClick = {
                                menuExpanded = false
                                onShareClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                menuExpanded = false
                                onDeleteClick()
                            }
                        )
                    }
                }
                Text(
                    text = relativeTime,
                    color = neu.textSecondary,
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp)
                )
            }
        }
    }
}

/**
 * Compact Neumorphic Bottom Navigation Bar matching Screen 2 mockup:
 * Home, Tools, Projects, Settings.
 * Note: Scaffold already applies safeDrawing insets, so we do not double-pad navigationBars here.
 */
@Composable
fun EliteMatrixBottomNavigationBar(
    activeScreen: PrimaryScreen,
    onSelectTab: (PrimaryScreen) -> Unit
) {
    val neu = LocalNeumorphicColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .neumorphicRaised(cornerRadius = 24.dp, elevation = 4.dp)
            .padding(vertical = 6.dp, horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavTabItem(
                label = "Home",
                icon = if (activeScreen == PrimaryScreen.Home) Icons.Filled.Home else Icons.Outlined.Home,
                selected = activeScreen == PrimaryScreen.Home,
                testTag = "nav_tab_home",
                onClick = { onSelectTab(PrimaryScreen.Home) }
            )
            BottomNavTabItem(
                label = "Tools",
                icon = Icons.Outlined.Build,
                selected = activeScreen == PrimaryScreen.ToolsHub,
                testTag = "nav_tab_tools",
                onClick = { onSelectTab(PrimaryScreen.ToolsHub) }
            )
            BottomNavTabItem(
                label = "Projects",
                icon = Icons.Outlined.Folder,
                selected = activeScreen == PrimaryScreen.ProjectsHistory,
                testTag = "nav_tab_projects",
                onClick = { onSelectTab(PrimaryScreen.ProjectsHistory) }
            )
            BottomNavTabItem(
                label = "Settings",
                icon = Icons.Outlined.Settings,
                selected = activeScreen == PrimaryScreen.SettingsScreen,
                testTag = "nav_tab_settings",
                onClick = { onSelectTab(PrimaryScreen.SettingsScreen) }
            )
        }
    }
}

@Composable
private fun BottomNavTabItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    val neu = LocalNeumorphicColors.current
    val tint = if (selected) neu.accentPrimary else neu.textSecondary

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 3.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(1.dp))
        Text(
            text = label,
            color = tint,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 11.sp
            )
        )
    }
}
