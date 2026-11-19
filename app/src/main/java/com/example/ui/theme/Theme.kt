package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.example.data.model.ThemeMode

private val StaffTrackLightColorScheme = lightColorScheme(
    primary = RoyalBluePrimary,
    onPrimary = Color.White,
    primaryContainer = SecondaryLightBlue,
    onPrimaryContainer = SecondaryOnLightBlue,
    secondary = InfoCyan,
    onSecondary = Color.White,
    secondaryContainer = InfoCyanContainerLight,
    onSecondaryContainer = Color(0xFF0E4D5E),
    tertiary = LeavePurple,
    onTertiary = Color.White,
    tertiaryContainer = LeavePurpleContainerLight,
    onTertiaryContainer = Color(0xFF4A258C),
    background = SoftOffWhiteBg,
    onBackground = DarkNavyText,
    surface = CardWhite,
    onSurface = DarkNavyText,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = MutedSlateText,
    surfaceDim = Color(0xFFE9EEF7),
    surfaceBright = CardWhite,
    surfaceContainerLowest = CardWhite,
    surfaceContainerLow = Color(0xFFF9FBFE),
    surfaceContainer = SoftOffWhiteBg,
    surfaceContainerHigh = Color(0xFFEEF3FC),
    surfaceContainerHighest = SecondaryLightBlue,
    outline = LightOutline,
    outlineVariant = Color(0xFFE6ECF7),
    error = AbsentRed,
    onError = Color.White,
    errorContainer = AbsentRedContainerLight,
    onErrorContainer = AbsentRed
)

private val StaffTrackDarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = Color(0xFF0B1C47),
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = Color(0xFF042630),
    secondaryContainer = InfoCyanContainerDark,
    onSecondaryContainer = Color(0xFFBAEBF7),
    tertiary = Color(0xFFB394F0),
    onTertiary = Color(0xFF281054),
    tertiaryContainer = LeavePurpleContainerDark,
    onTertiaryContainer = Color(0xFFE6DCFC),
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceDim = DarkBackground,
    surfaceBright = Color(0xFF22304D),
    surfaceContainerLowest = Color(0xFF090E1A),
    surfaceContainerLow = Color(0xFF101728),
    surfaceContainer = DarkSurface,
    surfaceContainerHigh = DarkSurfaceVariant,
    surfaceContainerHighest = Color(0xFF263453),
    outline = DarkOutline,
    outlineVariant = Color(0xFF22304D),
    error = Color(0xFFF67B82),
    onError = Color(0xFF470C12),
    errorContainer = AbsentRedContainerDark,
    onErrorContainer = Color(0xFFFDC4C7)
)

@Immutable
data class StaffTrackExtendedColors(
    val royalBlue: Color,
    val royalBlueDark: Color,
    val lightBlue: Color,
    val softOffWhite: Color,
    val darkNavyText: Color,
    val success: Color,
    val successContainer: Color,
    val danger: Color,
    val dangerContainer: Color,
    val warning: Color,
    val warningContainer: Color,
    val info: Color,
    val infoContainer: Color,
    val leave: Color,
    val leaveContainer: Color
)

private val LightExtendedColors = StaffTrackExtendedColors(
    royalBlue = RoyalBluePrimary,
    royalBlueDark = RoyalBlueDark,
    lightBlue = SecondaryLightBlue,
    softOffWhite = SoftOffWhiteBg,
    darkNavyText = DarkNavyText,
    success = PresentGreen,
    successContainer = PresentGreenContainerLight,
    danger = AbsentRed,
    dangerContainer = AbsentRedContainerLight,
    warning = HalfDayOrange,
    warningContainer = HalfDayOrangeContainerLight,
    info = InfoCyan,
    infoContainer = InfoCyanContainerLight,
    leave = LeavePurple,
    leaveContainer = LeavePurpleContainerLight
)

private val DarkExtendedColors = StaffTrackExtendedColors(
    royalBlue = DarkPrimary,
    royalBlueDark = RoyalBluePrimary,
    lightBlue = DarkPrimaryContainer,
    softOffWhite = DarkBackground,
    darkNavyText = DarkOnSurface,
    success = PresentGreen,
    successContainer = PresentGreenContainerDark,
    danger = Color(0xFFF67B82),
    dangerContainer = AbsentRedContainerDark,
    warning = HalfDayOrange,
    warningContainer = HalfDayOrangeContainerDark,
    info = DarkSecondary,
    infoContainer = InfoCyanContainerDark,
    leave = Color(0xFFB394F0),
    leaveContainer = LeavePurpleContainerDark
)

val LocalStaffTrackColors = staticCompositionLocalOf { LightExtendedColors }

val MaterialTheme.staffTrackColors: StaffTrackExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalStaffTrackColors.current

val StaffTrackShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(26.dp)
)

@Composable
fun StaffTrackTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> StaffTrackDarkColorScheme
        else -> StaffTrackLightColorScheme
    }

    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalStaffTrackColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = StaffTrackTypography,
            shapes = StaffTrackShapes,
            content = content
        )
    }
}
