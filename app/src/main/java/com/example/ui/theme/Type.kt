package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.model.AppFontFamily

val PlusJakartaSansFamily = FontFamily(
    Font(R.font.plus_jakarta_sans, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans, FontWeight.Bold)
)

val InterFontFamily = FontFamily(
    Font(R.font.inter, FontWeight.Normal),
    Font(R.font.inter, FontWeight.Medium),
    Font(R.font.inter, FontWeight.SemiBold),
    Font(R.font.inter, FontWeight.Bold)
)

fun typographyFor(appFont: AppFontFamily): Typography {
    val headingFamily: FontFamily
    val bodyFamily: FontFamily
    when (appFont) {
        AppFontFamily.PlusJakarta -> {
            headingFamily = PlusJakartaSansFamily
            bodyFamily = InterFontFamily
        }
        AppFontFamily.InterFont -> {
            headingFamily = InterFontFamily
            bodyFamily = InterFontFamily
        }
        AppFontFamily.SystemDefault -> {
            headingFamily = FontFamily.SansSerif
            bodyFamily = FontFamily.SansSerif
        }
        AppFontFamily.EditorialSerif -> {
            headingFamily = FontFamily.Serif
            bodyFamily = InterFontFamily
        }
        AppFontFamily.TechnicalMono -> {
            headingFamily = FontFamily.Monospace
            bodyFamily = FontFamily.Monospace
        }
    }

    return Typography(
        headlineLarge = TextStyle(
            fontFamily = headingFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            lineHeight = 32.sp,
            letterSpacing = (-0.3).sp
        ),
        headlineMedium = TextStyle(
            fontFamily = headingFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            letterSpacing = (-0.2).sp
        ),
        titleLarge = TextStyle(
            fontFamily = headingFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            lineHeight = 24.sp
        ),
        titleMedium = TextStyle(
            fontFamily = headingFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            lineHeight = 20.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            lineHeight = 21.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            lineHeight = 18.sp
        ),
        labelLarge = TextStyle(
            fontFamily = headingFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            lineHeight = 18.sp
        ),
        labelMedium = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp
        )
    )
}

val Typography = typographyFor(AppFontFamily.PlusJakarta)
