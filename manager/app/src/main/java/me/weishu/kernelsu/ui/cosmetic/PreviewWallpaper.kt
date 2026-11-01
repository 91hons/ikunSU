package me.weishu.kernelsu.ui.cosmetic

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale

/**
 * Draws the user background behind a theme preview so the mockup reflects the
 * real appearance instead of a flat placeholder.
 */
@Composable
fun PreviewWallpaper(modifier: Modifier = Modifier) {
    val state = LocalBackgroundState.current
    if (!state.enabled) return
    val bitmap = rememberBackgroundBitmap(state.uri) ?: return
    Box(modifier.fillMaxSize()) {
        Image(
            bitmap = bitmap,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        if (state.dim > 0f) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = state.dim)))
        }
    }
}