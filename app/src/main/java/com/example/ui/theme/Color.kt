package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.domain.model.ThemePalette

@Immutable
data class NeumorphicColors(
    val canvas: Color,
    val surfaceRaised: Color,
    val surfaceInset: Color,
    val shadowLight: Color,
    val shadowDark: Color,
    val accentPrimary: Color,
    val accentSecondary: Color,
    val accentSoftMintStart: Color,
    val accentSoftMintEnd: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val divider: Color,
    val sunAmber: Color,
    val dropletMint: Color,
    val thermoOrange: Color,
    val isDark: Boolean
)

val WarmIvoryLightColors = NeumorphicColors(
    canvas = Color(0xFFF4F2ED),
    surfaceRaised = Color(0xFFF8F7F3),
    surfaceInset = Color(0xFFEAE7E0),
    shadowLight = Color(0xFFFFFFFF),
    shadowDark = Color(0xFFD5D1C8),
    accentPrimary = Color(0xFF288862),
    accentSecondary = Color(0xFF1F6F4F),
    accentSoftMintStart = Color(0xFFCBEAD9),
    accentSoftMintEnd = Color(0xFFA6DEC2),
    textPrimary = Color(0xFF172620),
    textSecondary = Color(0xFF63706A),
    divider = Color(0xFFE3DFD7),
    sunAmber = Color(0xFFF0AD4E),
    dropletMint = Color(0xFF3FA77E),
    thermoOrange = Color(0xFFE89846),
    isDark = false
)

fun paletteFor(palette: ThemePalette, forceDark: Boolean): NeumorphicColors {
    if (forceDark || palette.isDarkPalette) {
        return when (palette) {
            ThemePalette.Midnight -> NeumorphicColors(
                canvas = Color(0xFF111822),
                surfaceRaised = Color(0xFF182230),
                surfaceInset = Color(0xFF0C1118),
                shadowLight = Color(0xFF223042),
                shadowDark = Color(0xFF070B10),
                accentPrimary = Color(0xFF34A87A),
                accentSecondary = Color(0xFF25855E),
                accentSoftMintStart = Color(0xFF1E3F35),
                accentSoftMintEnd = Color(0xFF163028),
                textPrimary = Color(0xFFECF3EF),
                textSecondary = Color(0xFF97A9A0),
                divider = Color(0xFF233040),
                sunAmber = Color(0xFFF5B759),
                dropletMint = Color(0xFF4AC292),
                thermoOrange = Color(0xFFF09D51),
                isDark = true
            )
            ThemePalette.ObsidianMint -> NeumorphicColors(
                canvas = Color(0xFF0F1915),
                surfaceRaised = Color(0xFF16241E),
                surfaceInset = Color(0xFF09100D),
                shadowLight = Color(0xFF20332B),
                shadowDark = Color(0xFF050A08),
                accentPrimary = Color(0xFF2CD48D),
                accentSecondary = Color(0xFF1CA36A),
                accentSoftMintStart = Color(0xFF183B2D),
                accentSoftMintEnd = Color(0xFF102A20),
                textPrimary = Color(0xFFEAF7F1),
                textSecondary = Color(0xFF8EB5A3),
                divider = Color(0xFF1E332A),
                sunAmber = Color(0xFFF7BC58),
                dropletMint = Color(0xFF3FE09C),
                thermoOrange = Color(0xFFF29F50),
                isDark = true
            )
            ThemePalette.CyberViolet -> NeumorphicColors(
                canvas = Color(0xFF161324),
                surfaceRaised = Color(0xFF1F1B33),
                surfaceInset = Color(0xFF0F0D1A),
                shadowLight = Color(0xFF2B2547),
                shadowDark = Color(0xFF090710),
                accentPrimary = Color(0xFF8B6CEF),
                accentSecondary = Color(0xFF6B4CD1),
                accentSoftMintStart = Color(0xFF2C2352),
                accentSoftMintEnd = Color(0xFF211A3E),
                textPrimary = Color(0xFFF2EFFC),
                textSecondary = Color(0xFFA79EC4),
                divider = Color(0xFF2B2544),
                sunAmber = Color(0xFFF6B95E),
                dropletMint = Color(0xFF53C99D),
                thermoOrange = Color(0xFFF29659),
                isDark = true
            )
            ThemePalette.MochaEspresso -> NeumorphicColors(
                canvas = Color(0xFF1E1916),
                surfaceRaised = Color(0xFF28211D),
                surfaceInset = Color(0xFF14110E),
                shadowLight = Color(0xFF362D28),
                shadowDark = Color(0xFF0C0A08),
                accentPrimary = Color(0xFFD48954),
                accentSecondary = Color(0xFFB36B39),
                accentSoftMintStart = Color(0xFF3D2C20),
                accentSoftMintEnd = Color(0xFF2E2017),
                textPrimary = Color(0xFFF5EFEA),
                textSecondary = Color(0xFFB3A398),
                divider = Color(0xFF332A24),
                sunAmber = Color(0xFFF4B955),
                dropletMint = Color(0xFF4BB88E),
                thermoOrange = Color(0xFFEE924F),
                isDark = true
            )
            else -> NeumorphicColors(
                canvas = Color(0xFF1B201E),
                surfaceRaised = Color(0xFF232A27),
                surfaceInset = Color(0xFF141816),
                shadowLight = Color(0xFF2E3733),
                shadowDark = Color(0xFF0E1110),
                accentPrimary = Color(0xFF33A376),
                accentSecondary = Color(0xFF24815B),
                accentSoftMintStart = Color(0xFF234236),
                accentSoftMintEnd = Color(0xFF1B332A),
                textPrimary = Color(0xFFEDF2EF),
                textSecondary = Color(0xFF9AA8A1),
                divider = Color(0xFF2D3531),
                sunAmber = Color(0xFFF3B354),
                dropletMint = Color(0xFF47BD8E),
                thermoOrange = Color(0xFFEE9A4C),
                isDark = true
            )
        }
    }

    return when (palette) {
        ThemePalette.SoftMint -> WarmIvoryLightColors.copy(
            canvas = Color(0xFFEEF6F1),
            surfaceRaised = Color(0xFFF5FAF7),
            surfaceInset = Color(0xFFE1EDE6),
            shadowDark = Color(0xFFCBDCD2)
        )
        ThemePalette.EmeraldMist -> WarmIvoryLightColors.copy(
            canvas = Color(0xFFE8F4EE),
            surfaceRaised = Color(0xFFF1F9F5),
            surfaceInset = Color(0xFFD9EBE2),
            shadowDark = Color(0xFFC2DBD0),
            accentPrimary = Color(0xFF1F7E59),
            accentSecondary = Color(0xFF166143),
            accentSoftMintStart = Color(0xFFBDE6D1),
            accentSoftMintEnd = Color(0xFF95D5B6)
        )
        ThemePalette.Cream -> WarmIvoryLightColors.copy(
            canvas = Color(0xFFF7F3E8),
            surfaceRaised = Color(0xFFFAF7F0),
            surfaceInset = Color(0xFFEDE7D8),
            shadowDark = Color(0xFFDAD2C0)
        )
        ThemePalette.Lavender -> WarmIvoryLightColors.copy(
            canvas = Color(0xFFF2F0F7),
            surfaceRaised = Color(0xFFF7F5FB),
            surfaceInset = Color(0xFFE6E2F0),
            shadowDark = Color(0xFFD2CCE0),
            accentPrimary = Color(0xFF5D54A4),
            accentSecondary = Color(0xFF484088),
            accentSoftMintStart = Color(0xFFDCD7F4),
            accentSoftMintEnd = Color(0xFFC6BEF0)
        )
        ThemePalette.Sky -> WarmIvoryLightColors.copy(
            canvas = Color(0xFFEEF4F8),
            surfaceRaised = Color(0xFFF4F8FC),
            surfaceInset = Color(0xFFE1EAF2),
            shadowDark = Color(0xFFCAD8E5),
            accentPrimary = Color(0xFF2B7CA8),
            accentSecondary = Color(0xFF1F6388),
            accentSoftMintStart = Color(0xFFCBE4F5),
            accentSoftMintEnd = Color(0xFFADD3EC)
        )
        ThemePalette.Peach -> WarmIvoryLightColors.copy(
            canvas = Color(0xFFF8F1EC),
            surfaceRaised = Color(0xFFFCF7F4),
            surfaceInset = Color(0xFFEEE3DC),
            shadowDark = Color(0xFFDDD0C7),
            accentPrimary = Color(0xFFC86446),
            accentSecondary = Color(0xFFA84E33),
            accentSoftMintStart = Color(0xFFF5D5C8),
            accentSoftMintEnd = Color(0xFFEEC0AE)
        )
        ThemePalette.RoseQuartz -> WarmIvoryLightColors.copy(
            canvas = Color(0xFFF8EFF2),
            surfaceRaised = Color(0xFFFCF6F8),
            surfaceInset = Color(0xFFEEE0E5),
            shadowDark = Color(0xFFDBC9CF),
            accentPrimary = Color(0xFFB84A6E),
            accentSecondary = Color(0xFF963857),
            accentSoftMintStart = Color(0xFFF4D2DD),
            accentSoftMintEnd = Color(0xFFEBBAC9)
        )
        ThemePalette.Sand -> WarmIvoryLightColors.copy(
            canvas = Color(0xFFF1ECE1),
            surfaceRaised = Color(0xFFF7F3EB),
            surfaceInset = Color(0xFFE5DEC8),
            shadowDark = Color(0xFFD1C9B8)
        )
        ThemePalette.MatchaZen -> WarmIvoryLightColors.copy(
            canvas = Color(0xFFEFF3EA),
            surfaceRaised = Color(0xFFF6F9F2),
            surfaceInset = Color(0xFFE2E8DA),
            shadowDark = Color(0xFFCCD5C2),
            accentPrimary = Color(0xFF4B833B),
            accentSecondary = Color(0xFF39682C),
            accentSoftMintStart = Color(0xFFD2E7C8),
            accentSoftMintEnd = Color(0xFFB9DAA9)
        )
        ThemePalette.ArcticPearl -> WarmIvoryLightColors.copy(
            canvas = Color(0xFFF1F4F7),
            surfaceRaised = Color(0xFFF8FAFC),
            surfaceInset = Color(0xFFE3E8EE),
            shadowDark = Color(0xFFCDD5DF),
            accentPrimary = Color(0xFF287D7D),
            accentSecondary = Color(0xFF1D6161),
            accentSoftMintStart = Color(0xFFC8E7E7),
            accentSoftMintEnd = Color(0xFFAAD8D8)
        )
        else -> WarmIvoryLightColors
    }
}

val LocalNeumorphicColors = staticCompositionLocalOf { WarmIvoryLightColors }
