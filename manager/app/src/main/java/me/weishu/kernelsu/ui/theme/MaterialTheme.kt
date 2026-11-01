package me.weishu.kernelsu.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowInsetsControllerCompat
import me.weishu.kernelsu.ui.webui.MonetColorsProvider

@Composable
fun MaterialKernelSUTheme(
    appSettings: AppSettings,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemDarkTheme = isSystemInDarkTheme()
    val darkTheme = appSettings.colorMode.isDark || (appSettings.colorMode.isSystem && systemDarkTheme)
    val amoledMode = appSettings.colorMode.isAmoled
    val dynamicColor = appSettings.keyColor == 0

    val colorScheme = rememberKernelSUColorScheme(
        seedColor = if (dynamicColor) Color.Unspecified else Color(appSettings.keyColor),
        isDark = darkTheme,
        isAmoled = amoledMode,
        paletteStyle = appSettings.paletteStyle,
        colorSpec = appSettings.colorSpec,
    )

    LaunchedEffect(darkTheme) {
        val window = (context as? Activity)?.window ?: return@LaunchedEffect
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = !darkTheme
            isAppearanceLightNavigationBars = !darkTheme
        }
    }

    val animatedColorScheme = colorScheme.animateAsState()
    val themedColorScheme = if (me.weishu.kernelsu.ui.cosmetic.LocalBackgroundState.current.enabled) {
        animatedColorScheme.withBackgroundTranslucency(me.weishu.kernelsu.ui.cosmetic.LocalBackgroundState.current.alpha)
    } else {
        animatedColorScheme
    }

    MaterialExpressiveTheme(
        colorScheme = themedColorScheme,
        motionScheme = MotionScheme.expressive(),
        typography = Typography,
        content = {
            MonetColorsProvider.UpdateCss(colorScheme)
            content()
        }
    )
}

/**
 * Lets the user background show through the chrome: surfaces keep their colour
 * but lose some opacity.
 */
private fun ColorScheme.withBackgroundTranslucency(alpha: Float): ColorScheme = copy(
    surface = surface.copy(alpha = (alpha + 0.04f).coerceAtMost(1f)),
    surfaceContainer = surfaceContainer.copy(alpha = alpha.coerceAtMost(1f)),
    surfaceContainerHigh = surfaceContainerHigh.copy(alpha = (alpha + 0.04f).coerceAtMost(1f)),
    surfaceContainerHighest = surfaceContainerHighest.copy(alpha = (alpha + 0.08f).coerceAtMost(1f)),
    surfaceContainerLow = surfaceContainerLow.copy(alpha = (alpha - 0.04f).coerceAtLeast(0.2f)),
    surfaceContainerLowest = surfaceContainerLowest.copy(alpha = (alpha - 0.06f).coerceAtLeast(0.2f)),
)