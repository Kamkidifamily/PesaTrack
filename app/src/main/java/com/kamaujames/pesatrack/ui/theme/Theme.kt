package com.kamaujames.pesatrack.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/** Colours Material doesn't have a slot for. Use as MaterialTheme.pesa.income etc. */
@Immutable
data class PesaExtraColors(
    val income: Color,
    val expense: Color,
    val warning: Color,
    val warningContainer: Color
)

private val LightExtras = PesaExtraColors(
    income = LightIncome,
    expense = LightExpense,
    warning = LightWarning,
    warningContainer = LightWarningContainer
)

private val DarkExtras = PesaExtraColors(
    income = DarkIncome,
    expense = DarkExpense,
    warning = DarkWarning,
    warningContainer = DarkWarningContainer
)

val LocalPesaColors = staticCompositionLocalOf { LightExtras }

val MaterialTheme.pesa: PesaExtraColors
    @Composable
    @ReadOnlyComposable
    get() = LocalPesaColors.current

private val LightScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = Color(0xFFD7F0E1),
    onPrimaryContainer = Color(0xFF0B3D24),
    secondary = LightIncome,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2E7),
    onSecondaryContainer = Color(0xFF0B3D24),
    background = LightBackground,
    onBackground = LightText,
    surface = LightSurface,
    onSurface = LightText,
    surfaceVariant = LightSurfaceHigh,
    onSurfaceVariant = LightTextSecondary,
    surfaceContainerLow = LightSurface,
    surfaceContainer = LightSurface,
    surfaceContainerHigh = LightSurface,
    surfaceContainerHighest = LightSurfaceHigh,
    outline = Color(0xFF646F68),          // was 0xFFB9C2BC (1.83:1) -> 4.6 - 5.2:1
    outlineVariant = LightBorder,
    error = LightExpense,
    onError = Color.White,                // 5.62:1 on LightExpense
    errorContainer = Color(0xFFFDE0DC),
    onErrorContainer = Color(0xFF5A0F0B),
    inverseSurface = Color(0xFF2B322D),
    inverseOnSurface = Color(0xFFEDF2EE),
    inversePrimary = Color(0xFF8FD9AE)
)

private val DarkScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = Color(0xFF123D27),
    onPrimaryContainer = Color(0xFFB7F0D0),
    secondary = DarkIncome,
    onSecondary = DarkOnPrimary,
    secondaryContainer = Color(0xFF1B4A31),
    onSecondaryContainer = Color(0xFFB7F0D0),
    background = DarkBackground,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = DarkSurfaceHigh,
    onSurfaceVariant = DarkTextSecondary,
    surfaceContainerLow = DarkSurface,
    surfaceContainer = DarkSurfaceHigh,
    surfaceContainerHigh = DarkSurfaceHigh,
    surfaceContainerHighest = DarkSurfaceHigh,
    outline = Color(0xFF8A948D),          // was 0xFF3A423D (1.73:1) -> 5.3 - 5.7:1
    outlineVariant = DarkBorder,
    error = DarkExpense,
    onError = Color(0xFF3B0A06),          // white on DarkExpense is only 3.0:1; this is 5.68:1
    errorContainer = Color(0xFF5C1A15),
    onErrorContainer = Color(0xFFFFDAD6),
    inverseSurface = Color(0xFFE1E8E3),
    inverseOnSurface = Color(0xFF1B211D),
    inversePrimary = Color(0xFF1E6B45)
)

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun PesaTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Keep status/navigation bar icons readable against the current theme.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = view.context.findActivity()?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalPesaColors provides if (darkTheme) DarkExtras else LightExtras
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkScheme else LightScheme,
            typography = Typography
        ) {
            // Sets the background AND the default content colour (LocalContentColor),
            // so plain Text/Icon follow the theme instead of defaulting to black.
            Surface(
                color = MaterialTheme.colorScheme.background,
                content = content
            )
        }
    }
}