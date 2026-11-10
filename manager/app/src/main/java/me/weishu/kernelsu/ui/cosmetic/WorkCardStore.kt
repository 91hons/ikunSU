package me.weishu.kernelsu.ui.cosmetic

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Customisation of the "working" status card on the home page: the headline
 * suffix and an optional background picture.
 */
@Immutable
data class WorkCardState(
    val text: String = "",
    val background: String = "",
) {
    val hasBackground: Boolean get() = background.isNotEmpty()
}

object WorkCardStore {
    const val DEFAULT_TEXT = "蔡徐坤唱跳中"

    private const val PREFS = "ikun_work_card"
    private const val KEY_TEXT = "text"
    private const val KEY_BACKGROUND = "background"

    private val _state = MutableStateFlow(WorkCardState())

    val state: StateFlow<WorkCardState> = _state

    fun init(context: Context) {
        _state.value = load(context)
    }

    fun load(context: Context): WorkCardState {
        val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return WorkCardState(
            text = prefs.getString(KEY_TEXT, "").orEmpty(),
            background = prefs.getString(KEY_BACKGROUND, "").orEmpty(),
        )
    }

    /** Text to show; empty means "use the built in string". */
    fun effectiveText(context: Context): String =
        _state.value.text.ifEmpty { load(context).text }

    fun setText(context: Context, text: String) {
        val app = context.applicationContext
        app.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_TEXT, text)
            .apply()
        _state.value = _state.value.copy(text = text)
    }

    fun setBackground(context: Context, uri: Uri?) {
        val app = context.applicationContext
        if (uri == null) {
            app.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .remove(KEY_BACKGROUND)
                .apply()
            _state.value = _state.value.copy(background = "")
            return
        }
        if (uri.scheme == "content") {
            runCatching {
                app.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
        }
        app.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_BACKGROUND, uri.toString())
            .apply()
        _state.value = _state.value.copy(background = uri.toString())
    }

    fun reset(context: Context) {
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .clear()
            .apply()
        _state.value = WorkCardState()
    }
}
