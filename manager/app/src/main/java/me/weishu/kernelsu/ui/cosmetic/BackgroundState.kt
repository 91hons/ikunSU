package me.weishu.kernelsu.ui.cosmetic

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * User-chosen app background.
 *
 * The URI is persisted together with a read grant, so the image keeps rendering
 * after a reboot without asking for it again.
 */
@Immutable
data class BackgroundState(
    val uri: String = "",
    val dim: Float = 0.25f,
) {
    val enabled: Boolean get() = uri.isNotEmpty()
}

val LocalBackgroundState = staticCompositionLocalOf { BackgroundState() }

object BackgroundStore {
    private val _state = MutableStateFlow(BackgroundState())

    /** Observable state: MainActivity collects it, settings only mutate it. */
    val state: StateFlow<BackgroundState> = _state

    fun init(context: Context) {
        _state.value = load(context)
    }
    private const val PREFS = "ikun_background"
    private const val KEY_URI = "uri"
    private const val KEY_DIM = "dim"
    private const val DEFAULT_DIM = 0.25f

    fun load(context: Context): BackgroundState {
        val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return BackgroundState(
            uri = prefs.getString(KEY_URI, "").orEmpty(),
            dim = prefs.getFloat(KEY_DIM, DEFAULT_DIM),
        )
    }

    fun setUri(context: Context, uri: Uri?) {
        val app = context.applicationContext
        val prefs = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (uri == null) {
            prefs.edit().remove(KEY_URI).apply()
            _state.value = BackgroundState(dim = prefs.getFloat(KEY_DIM, DEFAULT_DIM))
            return
        }
        runCatching {
            app.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        }
        prefs.edit().putString(KEY_URI, uri.toString()).apply()
        _state.value = BackgroundState(uri.toString(), prefs.getFloat(KEY_DIM, DEFAULT_DIM))
    }

    fun setDim(context: Context, dim: Float) {
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putFloat(KEY_DIM, dim.coerceIn(0f, 0.7f)).apply()
        _state.value = _state.value.copy(dim = dim.coerceIn(0f, 0.7f))
    }
}
