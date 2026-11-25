package com.kamaujames.pesatrack.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// Contrast ratios below are WCAG 2.x, measured against the background/surface noted.
// Targets: 4.5:1 for text, 3:1 for icons and component borders.

// ---- Dark theme ---------------------------------------------------------------
val DarkBackground = Color(0xFF0A0C0B)
val DarkSurface = Color(0xFF141816)
val DarkSurfaceHigh = Color(0xFF1B201D)
val DarkBorder = Color(0xFF252B27)           // decorative divider, intentionally subtle
val DarkText = Color(0xFFF1F4F2)             // 14.9 - 17.7
val DarkTextSecondary = Color(0xFF9AA59E)    // 6.5 - 7.7
val DarkPrimary = Color(0xFF2ED47A)          // 8.5 - 10.1
val DarkOnPrimary = Color(0xFF04140B)        // 9.7 on DarkPrimary
val DarkIncome = Color(0xFF2ED47A)
val DarkExpense = Color(0xFFFF5C6C)          // 5.5 - 6.5
val DarkWarning = Color(0xFFF5B83D)          // 9.3 - 11.0
val DarkWarningContainer = Color(0xFF33290F) // text on it: 12.9

// ---- Light theme --------------------------------------------------------------
val LightBackground = Color(0xFFF5F7F4)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceHigh = Color(0xFFEEF1EE)
val LightBorder = Color(0xFFE2E7E2)          // decorative divider, intentionally subtle
val LightText = Color(0xFF151D18)            // 15.1 - 17.2
val LightTextSecondary = Color(0xFF5E6A63)   // 4.96 - 5.65
val LightPrimary = Color(0xFF0F7A44)         // was 0xFF14854C (4.11 on surfaceHigh) -> 4.75 - 5.40
val LightOnPrimary = Color(0xFFFFFFFF)       // 5.40 on LightPrimary
val LightIncome = Color(0xFF0F7A44)          // same as LightPrimary
val LightExpense = Color(0xFFC42A3A)         // was 0xFFDC3D4B (3.83 on surfaceHigh) -> 4.93 - 5.62
val LightWarning = Color(0xFF946000)         // was 0xFFC98A0B (2.59 - 2.95) -> 4.69 - 5.34
val LightWarningContainer = Color(0xFFFFF2D9) // text on it: 15.5

// ---- Original names, now theme-aware -----------------------------------------
// These used to be fixed light-mode colours, so anything still using them stayed
// light-mode in dark mode (e.g. PrimaryText was 1.31:1 on the dark background).
// They are now read from the active theme, so existing call sites keep working.
// Only usable inside @Composable code. If the compiler complains about a
// @Composable invocation (Canvas / drawBehind lambda, remember { }, a top-level
// val), read the colour into a local val first, above that lambda.

val AppBackground: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.background

val CardWhite: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surface

val PrimaryText: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurface

val SecondaryText: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurfaceVariant

val MutedText: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.outline

val BorderColor: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.outlineVariant

val IncomeGreen: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.pesa.income

val ExpenseCoral: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.pesa.expense

val LightGreen: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primaryContainer

val SoftCoral: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.errorContainer

val SoftYellow: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.pesa.warningContainer

// Deliberately left fixed. Fine as a fill that carries white text (white on
// ForestGreen is about 10:1). If either is used as a TEXT or ICON colour, it is
// invisible on the dark background (1.5 - 2.0:1): use MaterialTheme.colorScheme.primary.
val ForestGreen = Color(0xFF164B3B)
val DeepGreen = Color(0xFF10382D)