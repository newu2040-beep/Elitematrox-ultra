package com.example.ui.theme

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.EnhancementAdjustments

/**
 * Soft modern neumorphic raised surface modifier:
 * Draws paired upper-left highlight and lower-right warm shadow with subtle border luminance.
 */
@Composable
fun Modifier.neumorphicRaised(
    cornerRadius: Dp = 20.dp,
    elevation: Dp = 5.dp,
    surfaceColor: Color? = null
): Modifier {
    val neu = LocalNeumorphicColors.current
    val fill = surfaceColor ?: neu.surfaceRaised
    val lightShadow = neu.shadowLight
    val darkShadow = neu.shadowDark
    val borderLight = if (neu.isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.75f)

    return this
        .drawBehind {
            val radiusPx = cornerRadius.toPx()
            val offsetPx = elevation.toPx()

            // Outer soft dark shadow (bottom-right)
            drawRoundRect(
                color = darkShadow.copy(alpha = if (neu.isDark) 0.55f else 0.42f),
                topLeft = Offset(offsetPx * 0.9f, offsetPx * 1.05f),
                size = Size(size.width, size.height),
                cornerRadius = CornerRadius(radiusPx, radiusPx)
            )
            drawRoundRect(
                color = darkShadow.copy(alpha = if (neu.isDark) 0.30f else 0.22f),
                topLeft = Offset(offsetPx * 1.5f, offsetPx * 1.7f),
                size = Size(size.width, size.height),
                cornerRadius = CornerRadius(radiusPx + 2f, radiusPx + 2f)
            )

            // Outer soft highlight (top-left)
            drawRoundRect(
                color = lightShadow.copy(alpha = if (neu.isDark) 0.25f else 0.88f),
                topLeft = Offset(-offsetPx * 0.75f, -offsetPx * 0.75f),
                size = Size(size.width, size.height),
                cornerRadius = CornerRadius(radiusPx, radiusPx)
            )
        }
        .clip(RoundedCornerShape(cornerRadius))
        .background(fill, RoundedCornerShape(cornerRadius))
        .border(1.dp, borderLight, RoundedCornerShape(cornerRadius))
}

/**
 * Recessed neutral surface for input wells, search bars, and inset tracks.
 */
@Composable
fun Modifier.neumorphicInset(
    cornerRadius: Dp = 16.dp
): Modifier {
    val neu = LocalNeumorphicColors.current
    return this
        .clip(RoundedCornerShape(cornerRadius))
        .background(neu.surfaceInset, RoundedCornerShape(cornerRadius))
        .border(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    neu.shadowDark.copy(alpha = 0.65f),
                    neu.shadowLight.copy(alpha = 0.75f)
                )
            ),
            shape = RoundedCornerShape(cornerRadius)
        )
}

@Composable
fun NeumorphicCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 22.dp,
    elevation: Dp = 5.dp,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val baseMod = modifier.neumorphicRaised(cornerRadius = cornerRadius, elevation = elevation)
    val clickMod = if (onClick != null) {
        baseMod.clickable(onClick = onClick)
    } else {
        baseMod
    }
    Box(
        modifier = clickMod.padding(contentPadding),
        content = content
    )
}

/**
 * Primary Emerald Green Pill Button (used for "Apply Changes", "Export Video", "Open Video").
 */
@Composable
fun EmeraldPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    testTag: String = ""
) {
    val neu = LocalNeumorphicColors.current
    val shape = RoundedCornerShape(28.dp)
    val gradient = Brush.horizontalGradient(
        colors = if (enabled) {
            listOf(
                neu.accentPrimary.copy(red = (neu.accentPrimary.red * 1.08f).coerceAtMost(1f)),
                neu.accentSecondary
            )
        } else {
            listOf(neu.textSecondary.copy(alpha = 0.4f), neu.textSecondary.copy(alpha = 0.4f))
        }
    )

    Box(
        modifier = modifier
            .let { if (testTag.isNotBlank()) it.testTag(testTag) else it }
            .fillMaxWidth()
            .height(52.dp)
            .minimumInteractiveComponentSize()
            .drawBehind {
                val r = 28.dp.toPx()
                drawRoundRect(
                    color = neu.accentSecondary.copy(alpha = 0.30f),
                    topLeft = Offset(0f, 5.dp.toPx()),
                    size = Size(size.width, size.height),
                    cornerRadius = CornerRadius(r, r)
                )
            }
            .clip(shape)
            .background(brush = gradient, shape = shape)
            .border(1.dp, Color.White.copy(alpha = 0.22f), shape)
            .clickable(enabled = enabled && !isLoading, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = Color.White,
                strokeWidth = 2.2.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (leadingIcon != null) {
                    leadingIcon()
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                )
            }
        }
    }
}

/**
 * Raised Warm-Ivory Neumorphic Pill Button (used for "Continue with Google", "Continue with Email",
 * "Guest Mode", "Share", "Go to Projects", "Advanced Options").
 */
@Composable
fun NeumorphicPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 50.dp,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    testTag: String = ""
) {
    val neu = LocalNeumorphicColors.current
    Box(
        modifier = modifier
            .let { if (testTag.isNotBlank()) it.testTag(testTag) else it }
            .fillMaxWidth()
            .height(height)
            .minimumInteractiveComponentSize()
            .neumorphicRaised(cornerRadius = height / 2, elevation = 4.dp)
            .clickable(enabled = enabled && !isLoading, onClick = onClick)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = neu.accentPrimary,
                strokeWidth = 2.2.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (leadingContent != null) {
                    leadingContent()
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Text(
                    text = text,
                    color = neu.textPrimary,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                )
                if (trailingContent != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    trailingContent()
                }
            }
        }
    }
}

/**
 * Selectable Pill Chip used on Export Settings (Resolution, Frame Rate, Video Codec, Bitrate, Format).
 */
@Composable
fun RowScope.NeumorphicChoicePill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    val neu = LocalNeumorphicColors.current
    val shape = RoundedCornerShape(22.dp)

    val boxMod = if (selected) {
        modifier
            .weight(1f)
            .let { if (testTag.isNotBlank()) it.testTag(testTag) else it }
            .height(42.dp)
            .drawBehind {
                val r = 22.dp.toPx()
                drawRoundRect(
                    color = neu.accentSecondary.copy(alpha = 0.28f),
                    topLeft = Offset(0f, 3.dp.toPx()),
                    size = Size(size.width, size.height),
                    cornerRadius = CornerRadius(r, r)
                )
            }
            .clip(shape)
            .background(
                brush = Brush.horizontalGradient(
                    listOf(neu.accentPrimary, neu.accentSecondary)
                ),
                shape = shape
            )
            .clickable(onClick = onClick)
    } else {
        modifier
            .weight(1f)
            .let { if (testTag.isNotBlank()) it.testTag(testTag) else it }
            .height(42.dp)
            .neumorphicRaised(cornerRadius = 22.dp, elevation = 3.dp)
            .clickable(onClick = onClick)
    }

    Box(
        modifier = boxMod,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else neu.textPrimary,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                fontSize = 13.sp
            )
        )
    }
}

/**
 * Adjustment Slider Row matching Screen 3 ("Brightness +15", "Contrast +20", "Saturation +10", "Temperature 0").
 */
@Composable
fun NeumorphicAdjustmentSliderRow(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    valueRange: IntRange = -100..100,
    testTag: String = ""
) {
    val neu = LocalNeumorphicColors.current
    val formattedValue = when {
        value > 0 -> "+$value"
        else -> "$value"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    color = neu.textPrimary,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.5.sp
                    )
                )
                Text(
                    text = formattedValue,
                    color = neu.textPrimary,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    )
                )
            }
            Slider(
                value = value.toFloat(),
                onValueChange = { onValueChange(it.toInt()) },
                valueRange = valueRange.first.toFloat()..valueRange.last.toFloat(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(26.dp)
                    .let { if (testTag.isNotBlank()) it.testTag(testTag) else it },
                colors = SliderDefaults.colors(
                    thumbColor = neu.surfaceRaised,
                    activeTrackColor = neu.accentPrimary,
                    inactiveTrackColor = neu.surfaceInset
                )
            )
        }
    }
}

/**
 * Top circular neumorphic ELITE MATRIX logo medallion (Screen 1).
 */
@Composable
fun EliteMatrixLogoMedallion(
    size: Dp = 76.dp
) {
    val neu = LocalNeumorphicColors.current
    Box(
        modifier = Modifier
            .size(size)
            .drawBehind {
                drawCircle(
                    color = neu.shadowDark.copy(alpha = 0.45f),
                    radius = this.size.minDimension / 2f,
                    center = Offset(center.x + 4.dp.toPx(), center.y + 5.dp.toPx())
                )
                drawCircle(
                    color = neu.shadowLight.copy(alpha = 0.9f),
                    radius = this.size.minDimension / 2f,
                    center = Offset(center.x - 4.dp.toPx(), center.y - 4.dp.toPx())
                )
            }
            .clip(CircleShape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFD9F1E4),
                        Color(0xFFB3E2CA)
                    )
                )
            )
            .border(1.5.dp, Color.White.copy(alpha = 0.7f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.46f)) {
            val w = this.size.width
            val h = this.size.height
            val path = Path().apply {
                moveTo(w * 0.22f, h * 0.14f)
                lineTo(w * 0.86f, h * 0.50f)
                lineTo(w * 0.22f, h * 0.86f)
                close()
            }
            drawPath(
                path = path,
                color = Color(0xFF174D38),
                style = Stroke(
                    width = w * 0.18f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}

/**
 * Center 3D Mint Play Prism resting on warm ivory pebble with mint wave sweep (Screen 1).
 */
@Composable
fun Neumorphic3DPlayHeroArtwork(
    modifier: Modifier = Modifier
) {
    val neu = LocalNeumorphicColors.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(145.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Soft diagonal mint wave sweep in background
            val wavePath = Path().apply {
                moveTo(0f, h * 0.75f)
                cubicTo(
                    w * 0.30f, h * 0.85f,
                    w * 0.62f, h * 0.20f,
                    w, h * 0.12f
                )
                lineTo(w, h * 0.82f)
                cubicTo(
                    w * 0.65f, h * 0.88f,
                    w * 0.35f, h * 0.98f,
                    0f, h * 0.95f
                )
                close()
            }
            drawPath(
                path = wavePath,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        neu.accentSoftMintStart.copy(alpha = 0.20f),
                        neu.accentSoftMintEnd.copy(alpha = 0.55f),
                        neu.accentSoftMintStart.copy(alpha = 0.25f)
                    )
                )
            )

            // Warm ivory pebble base shadow & body
            drawOval(
                color = neu.shadowDark.copy(alpha = 0.45f),
                topLeft = Offset(w * 0.25f, h * 0.26f),
                size = Size(w * 0.46f, h * 0.58f)
            )
            drawOval(
                brush = Brush.linearGradient(
                    colors = listOf(neu.surfaceRaised, neu.canvas)
                ),
                topLeft = Offset(w * 0.23f, h * 0.20f),
                size = Size(w * 0.46f, h * 0.58f)
            )

            // 3D Mint Play Triangle Shadow
            val cx = w * 0.45f
            val cy = h * 0.49f
            val r = h * 0.27f
            val shadowTriangle = Path().apply {
                moveTo(cx - r * 0.55f, cy - r * 0.82f + 10f)
                lineTo(cx + r * 0.92f, cy + 10f)
                lineTo(cx - r * 0.55f, cy + r * 0.82f + 10f)
                close()
            }
            drawPath(
                path = shadowTriangle,
                color = Color(0xFF287A58).copy(alpha = 0.26f),
                style = Stroke(width = 24f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // 3D Mint Play Triangle Extrusion (darker mint lower layer)
            val extrusionTriangle = Path().apply {
                moveTo(cx - r * 0.55f, cy - r * 0.82f + 4f)
                lineTo(cx + r * 0.92f, cy + 4f)
                lineTo(cx - r * 0.55f, cy + r * 0.82f + 4f)
                close()
            }
            drawPath(
                path = extrusionTriangle,
                color = Color(0xFF4EB38B),
                style = Stroke(width = 24f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // 3D Mint Play Triangle Top Highlight Face
            val topTriangle = Path().apply {
                moveTo(cx - r * 0.55f, cy - r * 0.82f)
                lineTo(cx + r * 0.92f, cy)
                lineTo(cx - r * 0.55f, cy + r * 0.82f)
                close()
            }
            drawPath(
                path = topTriangle,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF9BE2C3), Color(0xFF63C59D))
                ),
                style = Stroke(width = 22f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}

/**
 * Official 4-color Google 'G' mark for "Continue with Google".
 */
@Composable
fun GoogleLogoIcon(modifier: Modifier = Modifier.size(20.dp)) {
    Canvas(modifier = modifier) {
        val s = size.minDimension
        val stroke = s * 0.19f
        val inset = stroke / 2f
        val arcSize = Size(s - stroke, s - stroke)
        val topLeft = Offset(inset, inset)

        // Red top arc
        drawArc(
            color = Color(0xFFEA4335),
            startAngle = -145f,
            sweepAngle = 105f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Butt)
        )
        // Yellow left arc
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 145f,
            sweepAngle = 70f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Butt)
        )
        // Green bottom arc
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 45f,
            sweepAngle = 100f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Butt)
        )
        // Blue right arc + horizontal bar
        drawArc(
            color = Color(0xFF4285F4),
            startAngle = -10f,
            sweepAngle = 60f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Butt)
        )
        drawLine(
            color = Color(0xFF4285F4),
            start = Offset(s * 0.52f, s * 0.50f),
            end = Offset(s * 0.94f, s * 0.50f),
            strokeWidth = stroke,
            cap = StrokeCap.Butt
        )
    }
}

/**
 * TikTok / Social Media Preset Badge Icon (Screen 4).
 */
@Composable
fun SocialPresetBadgeIcon(presetName: String, modifier: Modifier = Modifier.size(38.dp)) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE5E2DA), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(22.dp)) {
            val w = size.width
            val h = size.height
            when {
                presetName.contains("Instagram", ignoreCase = true) -> {
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            listOf(Color(0xFFF58529), Color(0xFFDD2A7B), Color(0xFF8134AF))
                        ),
                        cornerRadius = CornerRadius(w * 0.28f, w * 0.28f),
                        style = Stroke(width = w * 0.14f)
                    )
                    drawCircle(
                        color = Color(0xFFDD2A7B),
                        radius = w * 0.22f
                    )
                }
                presetName.contains("YouTube", ignoreCase = true) -> {
                    drawRoundRect(
                        color = Color(0xFFFF0000),
                        cornerRadius = CornerRadius(w * 0.24f, w * 0.24f)
                    )
                    val play = Path().apply {
                        moveTo(w * 0.38f, h * 0.28f)
                        lineTo(w * 0.70f, h * 0.50f)
                        lineTo(w * 0.38f, h * 0.72f)
                        close()
                    }
                    drawPath(play, color = Color.White)
                }
                else -> {
                    // TikTok cyan + magenta + black musical note
                    val strokeW = w * 0.15f
                    drawCircle(
                        color = Color(0xFF25F4EE),
                        radius = w * 0.22f,
                        center = Offset(w * 0.36f, h * 0.68f),
                        style = Stroke(width = strokeW)
                    )
                    drawCircle(
                        color = Color(0xFFFE2C55),
                        radius = w * 0.22f,
                        center = Offset(w * 0.42f, h * 0.72f),
                        style = Stroke(width = strokeW)
                    )
                    drawCircle(
                        color = Color(0xFF111111),
                        radius = w * 0.22f,
                        center = Offset(w * 0.39f, h * 0.70f),
                        style = Stroke(width = strokeW)
                    )
                    drawLine(
                        color = Color(0xFF111111),
                        start = Offset(w * 0.58f, h * 0.70f),
                        end = Offset(w * 0.58f, h * 0.18f),
                        strokeWidth = strokeW,
                        cap = StrokeCap.Round
                    )
                    drawArc(
                        color = Color(0xFFFE2C55),
                        startAngle = 180f,
                        sweepAngle = 90f,
                        useCenter = false,
                        topLeft = Offset(w * 0.58f, h * 0.16f),
                        size = Size(w * 0.32f, h * 0.32f),
                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                    )
                }
            }
        }
    }
}

/**
 * Concentric soft-mint checkmark halo at the top of Screen 5 ("Processing Complete!").
 */
@Composable
fun ProcessingCompleteCheckmarkHero(
    modifier: Modifier = Modifier
) {
    val neu = LocalNeumorphicColors.current
    Box(
        modifier = modifier.size(164.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val c = center
            val maxR = size.minDimension / 2f

            // 4 decorative floating mint dots matching the mockup
            drawCircle(
                color = neu.accentPrimary.copy(alpha = 0.75f),
                radius = 3.5.dp.toPx(),
                center = Offset(c.x - maxR * 0.88f, c.y - maxR * 0.18f)
            )
            drawCircle(
                color = neu.accentPrimary.copy(alpha = 0.75f),
                radius = 4.dp.toPx(),
                center = Offset(c.x - maxR * 0.78f, c.y + maxR * 0.42f)
            )
            drawCircle(
                color = neu.accentPrimary.copy(alpha = 0.75f),
                radius = 3.5.dp.toPx(),
                center = Offset(c.x + maxR * 0.84f, c.y - maxR * 0.52f)
            )
            drawCircle(
                color = neu.accentPrimary.copy(alpha = 0.75f),
                radius = 3.5.dp.toPx(),
                center = Offset(c.x + maxR * 0.90f, c.y + maxR * 0.14f)
            )

            // Outer soft neumorphic mint ring
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        neu.accentSoftMintStart.copy(alpha = 0.65f),
                        neu.canvas.copy(alpha = 0.05f)
                    ),
                    center = c,
                    radius = maxR * 0.92f
                ),
                radius = maxR * 0.92f,
                center = c
            )

            // Middle raised ring
            drawCircle(
                color = neu.shadowDark.copy(alpha = 0.35f),
                radius = maxR * 0.66f,
                center = Offset(c.x + 4.dp.toPx(), c.y + 6.dp.toPx())
            )
            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFD9F2E5),
                        Color(0xFFB7E3CC)
                    )
                ),
                radius = maxR * 0.66f,
                center = c
            )

            // Inner emerald circle
            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF39A277),
                        Color(0xFF1F7452)
                    )
                ),
                radius = maxR * 0.46f,
                center = c
            )

            // Bold white checkmark
            val checkPath = Path().apply {
                moveTo(c.x - maxR * 0.17f, c.y + maxR * 0.01f)
                lineTo(c.x - maxR * 0.04f, c.y + maxR * 0.14f)
                lineTo(c.x + maxR * 0.19f, c.y - maxR * 0.12f)
            }
            drawPath(
                path = checkPath,
                color = Color.White,
                style = Stroke(
                    width = 4.5.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}

/**
 * Renders either the real extracted video frame bitmap (with live ColorMatrix filter applied)
 * or the signature Alpine Mountain Lake landscape matching the reference mockups.
 */
@Composable
fun VideoThumbnailPreview(
    thumbnailBitmap: Bitmap?,
    adjustments: EnhancementAdjustments = EnhancementAdjustments.Neutral,
    modifier: Modifier = Modifier
) {
    val colorMatrix = remember(adjustments) {
        val b = (adjustments.brightness / 100f) * 55f
        val c = 1f + (adjustments.contrast / 100f) * 0.65f
        val s = (1f + (adjustments.saturation / 100f) * 0.85f).coerceAtLeast(0f)
        val t = (adjustments.temperature / 100f) * 24f

        val saturationMatrix = ColorMatrix().apply { setToSaturation(s) }
        val scaleTranslateMatrix = ColorMatrix(
            floatArrayOf(
                c, 0f, 0f, 0f, b + t,
                0f, c, 0f, 0f, b,
                0f, 0f, c, 0f, b - t,
                0f, 0f, 0f, 1f, 0f
            )
        )
        saturationMatrix.timesAssign(scaleTranslateMatrix)
        saturationMatrix
    }

    if (thumbnailBitmap != null) {
        Image(
            bitmap = thumbnailBitmap.asImageBitmap(),
            contentDescription = "Video frame preview",
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.colorMatrix(colorMatrix),
            modifier = modifier
        )
    } else {
        // Painted Alpine Mountain Lake landscape matching the mockup's Nature_Video.mp4 preview
        val brightnessDelta = (adjustments.brightness / 100f) * 0.22f
        val tempDelta = (adjustments.temperature / 100f) * 0.15f
        Canvas(modifier = modifier) {
            val w = size.width
            val h = size.height

            fun tint(color: Color): Color {
                return Color(
                    red = (color.red + brightnessDelta + tempDelta).coerceIn(0f, 1f),
                    green = (color.green + brightnessDelta).coerceIn(0f, 1f),
                    blue = (color.blue + brightnessDelta - tempDelta).coerceIn(0f, 1f),
                    alpha = color.alpha
                )
            }

            // Sky gradient
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        tint(Color(0xFF4A90D9)),
                        tint(Color(0xFF8EC5FC)),
                        tint(Color(0xFFFAD09E))
                    ),
                    startY = 0f,
                    endY = h * 0.56f
                ),
                size = Size(w, h * 0.56f)
            )

            // Sun glow on horizon
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        tint(Color(0xFFFFF3C4)),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.48f, h * 0.42f),
                    radius = w * 0.35f
                ),
                radius = w * 0.35f,
                center = Offset(w * 0.48f, h * 0.42f)
            )

            // Distant snow-capped mountains
            val farMountains = Path().apply {
                moveTo(0f, h * 0.54f)
                lineTo(w * 0.16f, h * 0.28f)
                lineTo(w * 0.32f, h * 0.44f)
                lineTo(w * 0.50f, h * 0.22f)
                lineTo(w * 0.68f, h * 0.42f)
                lineTo(w * 0.85f, h * 0.24f)
                lineTo(w, h * 0.45f)
                lineTo(w, h * 0.56f)
                close()
            }
            drawPath(
                path = farMountains,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        tint(Color(0xFFEFF4FA)),
                        tint(Color(0xFF5D7694))
                    ),
                    startY = h * 0.22f,
                    endY = h * 0.56f
                )
            )

            // Pine forest slopes left & right
            val leftSlope = Path().apply {
                moveTo(0f, h * 0.38f)
                lineTo(w * 0.42f, h * 0.55f)
                lineTo(0f, h * 0.58f)
                close()
            }
            drawPath(leftSlope, color = tint(Color(0xFF2B5B3C)))

            val rightSlope = Path().apply {
                moveTo(w, h * 0.34f)
                lineTo(w * 0.52f, h * 0.55f)
                lineTo(w, h * 0.58f)
                close()
            }
            drawPath(rightSlope, color = tint(Color(0xFF366B45)))

            // Alpine lake reflection
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        tint(Color(0xFF7BB5E0)),
                        tint(Color(0xFF2E6B9E)),
                        tint(Color(0xFF1A466B))
                    ),
                    startY = h * 0.54f,
                    endY = h
                ),
                topLeft = Offset(0f, h * 0.54f),
                size = Size(w, h * 0.46f)
            )

            // Foreground pine silhouettes at bottom-left
            val fgPines = Path().apply {
                moveTo(0f, h)
                lineTo(0f, h * 0.68f)
                lineTo(w * 0.08f, h * 0.82f)
                lineTo(w * 0.14f, h * 0.65f)
                lineTo(w * 0.22f, h * 0.86f)
                lineTo(w * 0.28f, h * 0.74f)
                lineTo(w * 0.36f, h)
                close()
            }
            drawPath(fgPines, color = tint(Color(0xFF163826)))
        }
    }
}
