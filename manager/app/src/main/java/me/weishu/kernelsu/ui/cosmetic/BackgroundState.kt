package me.weishu.kernelsu.ui.cosmetic

import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

/**
 * User-chosen app background plus the knobs that shape it.
 *
 * The URI is persisted together with a read grant, so the image keeps rendering
 * after a reboot without asking for it again.
 */
@Immutable
data class BackgroundState(
    val uri: String = "",
    val dim: Float = 0.25f,
    val alpha: Float = 0.82f,
    val blur: Float = 0f,
) {
    val enabled: Boolean get() = uri.isNotEmpty()
}

val LocalBackgroundState = staticCompositionLocalOf { BackgroundState() }

object BackgroundStore {
    private const val PREFS = "ikun_background"
    private const val KEY_URI = "uri"
    private const val KEY_DIM = "dim"
    private const val KEY_ALPHA = "alpha"
    private const val KEY_BLUR = "blur"
    private const val DEFAULT_DIM = 0.25f
    private const val DEFAULT_ALPHA = 0.82f
    private const val DEFAULT_BLUR = 0f

    private val _state = MutableStateFlow(BackgroundState())

    /** Observable state: MainActivity collects it, settings only mutate it. */
    val state: StateFlow<BackgroundState> = _state

    fun init(context: Context) {
        _state.value = load(context)
    }

    fun load(context: Context): BackgroundState {
        val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return BackgroundState(
            uri = prefs.getString(KEY_URI, "").orEmpty(),
            dim = prefs.getFloat(KEY_DIM, DEFAULT_DIM),
            alpha = prefs.getFloat(KEY_ALPHA, DEFAULT_ALPHA),
            blur = prefs.getFloat(KEY_BLUR, DEFAULT_BLUR),
        )
    }

    private fun restore(context: Context, state: BackgroundState) {
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_URI, state.uri)
            .putFloat(KEY_DIM, state.dim)
            .putFloat(KEY_ALPHA, state.alpha)
            .putFloat(KEY_BLUR, state.blur)
            .apply()
        _state.value = state
    }

    fun setUri(context: Context, uri: Uri?) {
        val app = context.applicationContext
        if (uri == null) {
            restore(app, _state.value.copy(uri = ""))
            return
        }
        if (uri.scheme == "content") {
            runCatching {
                app.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
        }
        restore(app, _state.value.copy(uri = uri.toString()))
    }

    fun setDim(context: Context, dim: Float) {
        restore(context, _state.value.copy(dim = dim.coerceIn(0f, 0.7f)))
    }

    fun setAlpha(context: Context, alpha: Float) {
        restore(context, _state.value.copy(alpha = alpha.coerceIn(0.1f, 1f)))
    }

    fun setBlur(context: Context, blur: Float) {
        restore(context, _state.value.copy(blur = blur.coerceIn(0f, 25f)))
    }

    fun reset(context: Context) {
        restore(context, BackgroundState())
    }

    /**
     * Copies the current system wallpaper into app storage and uses it as the
     * background. Returns false when the wallpaper cannot be read.
     */
    // Reading the wallpaper needs a privileged permission on recent Android versions;
    // the call is wrapped and simply reports failure when the platform denies it.
    @SuppressLint("MissingPermission")
    fun useSystemWallpaper(context: Context): Boolean = runCatching {
        val app = context.applicationContext
        val drawable = WallpaperManager.getInstance(app).drawable ?: return false
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth.coerceAtLeast(1),
            drawable.intrinsicHeight.coerceAtLeast(1),
            Bitmap.Config.ARGB_8888,
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        val file = File(app.filesDir, "ikun_wallpaper.png")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 95, it) }
        setUri(app, Uri.fromFile(file))
        true
    }.getOrElse { readWallpaperWithRoot(context) }

    /**
     * Fallback for Android builds where the wallpaper API is locked behind a
     * privileged permission: with root we can copy the wallpaper blob straight
     * out of the system user directory.
     */
    private fun readWallpaperWithRoot(context: Context): Boolean = runCatching {
        val app = context.applicationContext
        val shell = com.topjohnwu.superuser.Shell.getShell()
        val candidates = listOf(
            "/data/system/users/0/wallpaper",
            "/data/system/users/0/wallpaper_orig",
        )
        for (path in candidates) {
            val suFile = com.topjohnwu.superuser.io.SuFile(path).apply { this.shell = shell }
            if (!suFile.isFile || suFile.length() <= 0) continue
            val bytes = com.topjohnwu.superuser.io.SuFileInputStream.open(suFile).use { it.readBytes() }
            if (setBackgroundBytes(app, bytes, "png")) return true
        }
        false
    }.getOrDefault(false)

    /** Replaces the background with raw image bytes (used by theme import). */
    fun setBackgroundBytes(context: Context, bytes: ByteArray, extension: String = "png"): Boolean =
        runCatching {
            val app = context.applicationContext
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
            if (bounds.outWidth <= 0) return false
            val file = File(app.filesDir, "ikun_theme_background.$extension")
            file.writeBytes(bytes)
            setUri(app, Uri.fromFile(file))
            true
        }.getOrDefault(false)
}
