package me.weishu.kernelsu.ui.screen.module

import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.topjohnwu.superuser.io.SuFile
import com.topjohnwu.superuser.io.SuFileInputStream
import kotlinx.coroutines.Dispatchers
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.withContext
import me.weishu.kernelsu.ui.util.getRootShell

/**
 * Optional per-module artwork.
 *
 * A module can ship a picture and point at it from `module.prop`:
 *
 *     banner=banner.png
 *
 * When the key is missing we fall back to the conventional file names below.
 * Everything is read through a root shell because `/data/adb/modules` is not
 * readable by the manager process.
 */
private const val MODULE_DIR = "/data/adb/modules/"
private const val MODULE_UPDATE_DIR = "/data/adb/modules_update/"
private const val MAX_BANNER_BYTES = 4L * 1024 * 1024
private const val BANNER_DECODE_PX = 720
private val FALLBACK_NAMES = listOf("banner.png", "banner.jpg", "banner.webp", "background.png")

private val bannerCache = LruCache<String, ImageBitmap>(32)

private fun readModulePropBanner(moduleId: String): String? = runCatching {
    val shell = getRootShell()
    for (dir in listOf(MODULE_DIR + moduleId, MODULE_UPDATE_DIR + moduleId)) {
        val prop = SuFile("$dir/module.prop").apply { this.shell = shell }
        if (!prop.isFile) continue
        val text = SuFileInputStream.open(prop).use { String(it.readBytes()) }
        val value = text.lineSequence()
            .map { it.trim() }
            .firstOrNull { it.startsWith("banner=") }
            ?.substringAfter('=')
            ?.trim()
        if (!value.isNullOrEmpty()) return value
    }
    null
}.getOrNull()

private fun fetchRemote(url: String): ByteArray? = runCatching {
    val connection = (URL(url).openConnection() as HttpURLConnection).apply {
        connectTimeout = 10_000
        readTimeout = 20_000
        instanceFollowRedirects = true
        setRequestProperty("User-Agent", "ikunSU")
    }
    try {
        if (connection.responseCode !in 200..299) return null
        if (connection.contentLengthLong > MAX_BANNER_BYTES) return null
        connection.inputStream.use { stream ->
            val bytes = stream.readBytes()
            if (bytes.size.toLong() > MAX_BANNER_BYTES) null else bytes
        }
    } finally {
        connection.disconnect()
    }
}.getOrNull()

private fun readBannerBytes(moduleId: String): ByteArray? = runCatching {
    val shell = getRootShell()
    val declared = readModulePropBanner(moduleId)
    if (!declared.isNullOrEmpty() &&
        (declared.startsWith("http://", true) || declared.startsWith("https://", true))
    ) {
        return fetchRemote(declared)
    }
    val candidates = buildList {
        if (!declared.isNullOrEmpty() && !declared.split('/').contains("..")) {
            if (declared.startsWith("/")) {
                add(declared)
            } else {
                add(MODULE_DIR + moduleId + "/" + declared)
                add(MODULE_UPDATE_DIR + moduleId + "/" + declared)
            }
        }
        FALLBACK_NAMES.forEach {
            add(MODULE_DIR + moduleId + "/" + it)
            add(MODULE_UPDATE_DIR + moduleId + "/" + it)
        }
    }
    val file = candidates
        .map { SuFile(it).apply { this.shell = shell } }
        .firstOrNull { it.isFile && it.length() in 1..MAX_BANNER_BYTES }
        ?: return null
    SuFileInputStream.open(file).use { it.readBytes() }
}.getOrNull()

private fun decodeBanner(bytes: ByteArray): ImageBitmap? = runCatching {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
    var sample = 1
    while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= BANNER_DECODE_PX) sample *= 2
    BitmapFactory.decodeByteArray(
        bytes, 0, bytes.size,
        BitmapFactory.Options().apply { inSampleSize = sample },
    )?.asImageBitmap()
}.getOrNull()

@Composable
fun rememberModuleBanner(moduleId: String): ImageBitmap? {
    val cached = bannerCache.get(moduleId)
    if (cached != null) return cached
    val bitmap by produceState<ImageBitmap?>(initialValue = null, key1 = moduleId) {
        value = withContext(Dispatchers.IO) {
            readBannerBytes(moduleId)?.let(::decodeBanner)?.also { bannerCache.put(moduleId, it) }
        }
    }
    return bitmap
}

/**
 * Banner strip drawn at the top of a module card. Renders nothing when the
 * module does not ship artwork, so the card keeps its usual look.
 */
@Composable
fun ModuleBanner(moduleId: String, height: androidx.compose.ui.unit.Dp = 96.dp) {
    val bitmap = rememberModuleBanner(moduleId) ?: return
    Box(
        Modifier
            .fillMaxWidth()
            .height(height),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            bitmap = bitmap,
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().height(height),
            contentScale = ContentScale.Crop,
        )
        Box(
            Modifier
                .fillMaxWidth()
                .height(height)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.35f)),
                    ),
                ),
        )
    }
}

/**
 * Draws the module artwork behind the card content, covering the whole card.
 * A horizontal scrim keeps the text readable on bright pictures.
 */
fun Modifier.moduleCardBackground(banner: ImageBitmap?, scrim: Color, cornerRadius: androidx.compose.ui.unit.Dp = 16.dp): Modifier {
    if (banner == null) return this
    return clip(androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius)).drawWithCache {
        val scale = maxOf(size.width / banner.width, size.height / banner.height)
        val srcW = (size.width / scale).roundToInt().coerceIn(1, banner.width)
        val srcH = (size.height / scale).roundToInt().coerceIn(1, banner.height)
        val srcOffset = IntOffset((banner.width - srcW) / 2, (banner.height - srcH) / 2)
        val srcSize = IntSize(srcW, srcH)
        val dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt())
        val gradient = Brush.horizontalGradient(
            listOf(scrim.copy(alpha = 0.94f), scrim.copy(alpha = 0.82f), scrim.copy(alpha = 0.55f))
        )
        onDrawBehind {
            drawImage(image = banner, srcOffset = srcOffset, srcSize = srcSize, dstSize = dstSize)
            drawRect(gradient)
        }
    }
}