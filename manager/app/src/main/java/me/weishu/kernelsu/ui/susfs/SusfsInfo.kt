package me.weishu.kernelsu.ui.susfs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.weishu.kernelsu.ui.util.getRootShell

/** SUSFS state exposed to the home page. */
data class SusfsInfo(
    val loaded: Boolean = false,
    val version: String = "",
    val variant: String = "",
) {
    /** `支持 | v2.3.0 (GKI-LKM)` style summary, empty when not loaded. */
    val summary: String
        get() = when {
            !loaded -> ""
            variant.isNotEmpty() -> "$version ($variant)"
            else -> version
        }
}

private fun readInfo(ksud: String): SusfsInfo = runCatching {
    val out = ArrayList<String>()
    getRootShell().newJob().add("$ksud susfs status").to(out, null).exec()
    val fields = out.associate {
        val index = it.indexOf('=')
        if (index > 0) it.substring(0, index) to it.substring(index + 1) else it to ""
    }
    SusfsInfo(
        loaded = fields["loaded"] == "true",
        version = fields["version"].orEmpty(),
        variant = fields["variant"].orEmpty(),
    )
}.getOrDefault(SusfsInfo())

/**
 * Reads the SUSFS status once per composition. Returns [SusfsInfo.Empty] while
 * loading and when the module is not loaded, so callers can hide the row.
 */
@Composable
fun rememberSusfsInfo(): SusfsInfo {
    val context = LocalContext.current
    val ksud = remember { context.applicationInfo.nativeLibraryDir + "/libksud.so" }
    var info by remember { mutableStateOf(SusfsInfo()) }
    LaunchedEffect(ksud) {
        info = withContext(Dispatchers.IO) { readInfo(ksud) }
    }
    return info
}
