package me.weishu.kernelsu.ui.susfs

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.component.material.SegmentedSwitchItem
import me.weishu.kernelsu.ui.util.getRootShell
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * SUSFS state as seen by the Manager.
 *
 * The module is loaded and unloaded through ksud, which detects the kernel KMI by
 * itself, so the Manager only has to flip a switch.
 */
private data class SusfsStatus(
    val loaded: Boolean = false,
    val autoload: Boolean = false,
    val version: String = "",
)

private fun readStatus(ksud: String): SusfsStatus = runCatching {
    val status = ArrayList<String>()
    getRootShell().newJob().add("$ksud susfs status").to(status, null).exec()
    val autoload = ArrayList<String>()
    getRootShell().newJob().add("$ksud susfs autoload status").to(autoload, null).exec()
    val fields = status.associate {
        val index = it.indexOf('=')
        if (index > 0) it.substring(0, index) to it.substring(index + 1) else it to ""
    }
    SusfsStatus(
        loaded = fields["loaded"] == "true",
        autoload = autoload.any { it.trim() == "autoload=true" },
        version = fields["version"].orEmpty(),
    )
}.getOrDefault(SusfsStatus())

private fun applySusfs(ksud: String, enable: Boolean): Boolean = runCatching {
    if (enable) {
        run("$ksud susfs autoload enable") && run("$ksud susfs load")
    } else {
        run("$ksud susfs unload") && run("$ksud susfs autoload disable")
    }
}.getOrDefault(false)

private fun run(command: String): Boolean =
    getRootShell().newJob().add(command).exec().isSuccess

private fun ksudPath(context: android.content.Context): String =
    context.applicationInfo.nativeLibraryDir + "/libksud.so"

@Composable
private fun rememberSusfs(): Triple<SusfsStatus, (Boolean) -> Unit, () -> Unit> {
    val context = LocalContext.current
    val ksud = remember { ksudPath(context) }
    var status by remember { mutableStateOf(SusfsStatus()) }
    val refresh: () -> Unit = {
        // Fire and forget: the recomposition follows the state update.
    }
    LaunchedEffect(Unit) {
        status = withContext(Dispatchers.IO) { readStatus(ksud) }
    }
    val setEnabled: (Boolean) -> Unit = { enable ->
        status = status.copy(loaded = enable)
        Thread {
            applySusfs(ksud, enable)
            status = readStatus(ksud)
        }.start()
    }
    return Triple(status, setEnabled, refresh)
}

@Composable
fun SusfsSwitchMaterial() {
    val (status, setEnabled, _) = rememberSusfs()
    SegmentedSwitchItem(
        icon = Icons.Filled.Shield,
        title = stringResource(R.string.ikun_susfs),
        summary = stringResource(
            R.string.ikun_susfs_summary,
            status.version.ifEmpty { "-" },
            if (status.autoload) "on" else "off",
        ),
        checked = status.loaded,
        onCheckedChange = setEnabled,
    )
}

@Composable
fun SusfsSwitchMiuix() {
    val (status, setEnabled, _) = rememberSusfs()
    val colorScheme = MiuixTheme.colorScheme
    SwitchPreference(
        title = stringResource(R.string.ikun_susfs),
        summary = stringResource(
            R.string.ikun_susfs_summary,
            status.version.ifEmpty { "-" },
            if (status.autoload) "on" else "off",
        ),
        startAction = {
            Icon(
                Icons.Filled.Shield,
                modifier = Modifier.padding(end = 6.dp),
                contentDescription = stringResource(R.string.ikun_susfs),
                tint = colorScheme.onBackground,
            )
        },
        checked = status.loaded,
        onCheckedChange = setEnabled,
    )
}
