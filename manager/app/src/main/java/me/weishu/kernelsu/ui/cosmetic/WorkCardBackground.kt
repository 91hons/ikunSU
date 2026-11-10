package me.weishu.kernelsu.ui.cosmetic

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Draws the user-picked work card picture behind [content].
 *
 * Renders nothing extra when no picture is configured, so the card keeps its
 * normal surface colour.
 */
@Composable
fun WorkCardBackground(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    scrim: Color = Color.Black,
    content: @Composable () -> Unit,
) {
    val state = WorkCardStore.state.value
    val bitmap = if (state.hasBackground) rememberBackgroundBitmap(state.background) else null
    if (bitmap == null) {
        Box(modifier) { content() }
        return
    }
    Box(modifier.clip(androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius))) {
        Image(
            bitmap = bitmap,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(Modifier.fillMaxSize().background(scrim.copy(alpha = 0.45f)))
        content()
    }
}
