package me.weishu.kernelsu.ui.cosmetic

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Renders the user background below the app content.
 *
 * The bitmap is decoded off the main thread and re-decoded only when the URI
 * changes, so switching between pages stays cheap.
 */
@Composable
fun BackgroundLayer(state: BackgroundState, modifier: Modifier = Modifier) {
    if (!state.enabled) return

    val context = LocalContext.current
    val bitmap by produceState<ImageBitmap?>(initialValue = null, key1 = state.uri) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                context.contentResolver.openInputStream(Uri.parse(state.uri))?.use { stream ->
                    BitmapFactory.decodeStream(stream)?.asImageBitmap()
                }
            }.getOrNull()
        }
    }

    val image = bitmap ?: return
    Box(modifier.fillMaxSize()) {
        Image(
            bitmap = image,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        if (state.dim > 0f) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = state.dim)))
        }
    }
}
