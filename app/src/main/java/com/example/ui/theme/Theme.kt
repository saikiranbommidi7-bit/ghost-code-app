package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView

private val GhostColorScheme = darkColorScheme(
    primary = PurplePrimary,
    secondary = IndigoPrimary,
    tertiary = CyanAccent,
    background = GhostDarkBg,
    surface = GhostCardBg,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = GhostDarkBg,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    surfaceVariant = GhostSubCardBg,
    onSurfaceVariant = TextSecondary,
    outline = GhostBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force premium dark modern theme as default
    content: @Composable () -> Unit,
) {
    val colorScheme = GhostColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = GhostDarkBg.toArgb()
            window.navigationBarColor = GhostDarkBg.toArgb()
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
