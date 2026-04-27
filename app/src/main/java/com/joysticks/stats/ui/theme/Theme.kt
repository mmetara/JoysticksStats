package com.joysticks.stats.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val HudBackground = Color(0xFF070A0E)
val HudBackgroundDeep = Color(0xFF030509)
val HudPanel = Color(0xE6141820)
val HudPanelSoft = Color(0xCC101620)
val HudBorder = Color(0x26FFFFFF)
val HudBorderActive = Color(0x994CAF50)
val FieldGreen = Color(0xFF4CAF50)
val FieldGreenLight = Color(0xFF7ED957)
val HudBlue = Color(0xFF4D86FF)
val HudRed = Color(0xFFFF4D57)
val HudOrange = Color(0xFFFF7A2F)
val ScoreYellow = Color(0xFFFFC857)
val ChalkWhite = Color(0xFFF4F7FB)
val HudMuted = Color(0xFF7C8593)
val DarkText = Color(0xFF101318)

private val BaseballColorScheme = darkColorScheme(

    primary = FieldGreen,
    secondary = HudBlue,
    tertiary = ScoreYellow,

    background = HudBackground,

    surface = HudPanel,
    surfaceVariant = HudPanelSoft,

    onPrimary = HudBackgroundDeep,
    onSecondary = ChalkWhite,
    onSurface = ChalkWhite,
    onSurfaceVariant = HudMuted,
    onBackground = ChalkWhite,
    error = HudRed,
    onError = ChalkWhite
)

private val HudTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 38.sp,
        lineHeight = 42.sp,
        letterSpacing = 1.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 32.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.5.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 24.sp,
        lineHeight = 28.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 22.sp,
        lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 22.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 13.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.8.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp
    )
)

@Composable
fun JoysticksStatsTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BaseballColorScheme,
        typography = HudTypography,
        content = content
    )
}
