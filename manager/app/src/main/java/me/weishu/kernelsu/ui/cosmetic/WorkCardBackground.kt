package me.weishu.kernelsu.ui.cosmetic

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
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
    val state by WorkCardStore.state.collectAsState()
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

/**
 * Paints the work card picture behind whatever the modifier is attached to,
 * clipped to [cornerRadius]. The host surface must be transparent for it to show.
 */
fun Modifier.workCardBackdrop(
    bitmap: androidx.compose.ui.graphics.ImageBitmap?,
    cornerRadius: Dp = 16.dp,
    scrim: Color = Color.Black,
): Modifier {
    if (bitmap == null) return this
    return this
        .clip(androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius))
        .then(
            Modifier.drawWithCache {
                val scale = maxOf(size.width / bitmap.width, size.height / bitmap.height)
                val srcW = (size.width / scale).toInt().coerceIn(1, bitmap.width)
                val srcH = (size.height / scale).toInt().coerceIn(1, bitmap.height)
                val srcOffset = androidx.compose.ui.unit.IntOffset((bitmap.width - srcW) / 2, (bitmap.height - srcH) / 2)
                val srcSize = androidx.compose.ui.unit.IntSize(srcW, srcH)
                val dstSize = androidx.compose.ui.unit.IntSize(size.width.toInt(), size.height.toInt())
                onDrawBehind {
                    drawImage(image = bitmap, srcOffset = srcOffset, srcSize = srcSize, dstSize = dstSize)
                    drawRect(scrim.copy(alpha = 0.45f))
                }
            }
        )
}