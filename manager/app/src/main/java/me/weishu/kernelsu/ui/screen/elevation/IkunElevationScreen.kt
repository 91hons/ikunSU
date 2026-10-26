package me.weishu.kernelsu.ui.screen.elevation

import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.LifecycleResumeEffect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.weishu.kernelsu.R
import me.weishu.kernelsu.core.elevation.ElevationBlocker
import me.weishu.kernelsu.core.elevation.ElevationDevice
import me.weishu.kernelsu.core.elevation.ElevationExploit
import me.weishu.kernelsu.core.elevation.ElevationPreferences
import me.weishu.kernelsu.core.elevation.ElevationProbe
import me.weishu.kernelsu.ui.LocalUiMode
import me.weishu.kernelsu.ui.UiMode
import me.weishu.kernelsu.ui.navigation3.LocalNavigator

/**
 * ikun提权: runs the DirtyFrag payload from inside the Manager.
 *
 * The screen only owns presentation and lifecycle. The device probe decides
 * whether the payload can run at all, and [ElevationExploit] does the actual
 * work off the main thread, streaming the native log back as it goes.
 */
@Composable
fun IkunElevationScreen() {
    val context = LocalContext.current
    val navigator = LocalNavigator.current
    val scope = rememberCoroutineScope()
    val preferences = remember { ElevationPreferences(context) }
    var probe by remember { mutableStateOf(ElevationDevice.probe(context)) }
    var disableModules by remember { mutableStateOf(preferences.disableModules) }
    var softReboot by remember { mutableStateOf(preferences.softReboot) }
    var running by remember { mutableStateOf(false) }
    var log by remember { mutableStateOf("") }
    var outcome by remember { mutableStateOf("") }
    var notice by remember { mutableStateOf("") }

    val blockedAbi = stringResource(R.string.ikun_elevation_blocked_abi)
    val blockedSecondaryUser = stringResource(R.string.ikun_elevation_blocked_secondary_user)
    val blockedKernel = stringResource(R.string.ikun_elevation_blocked_kernel)
    val blockedAssets = stringResource(R.string.ikun_elevation_blocked_assets)
    val blockedManager = stringResource(R.string.ikun_elevation_blocked_manager)
    val resultStarted = stringResource(R.string.ikun_elevation_result_started)
    val resultBootstrap = stringResource(R.string.ikun_elevation_result_bootstrap)
    val resultTimeout = stringResource(R.string.ikun_elevation_result_timeout)
    val resultPatch = stringResource(R.string.ikun_elevation_result_patch)
    val resultHooked = stringResource(R.string.ikun_elevation_result_hooked)
    val resultFailed = stringResource(R.string.ikun_elevation_operation_failed)
    val logCopied = stringResource(R.string.ikun_elevation_log_copied)
    val label = stringResource(R.string.ikun_elevation_title)

    LifecycleResumeEffect(Unit) {
        probe = ElevationDevice.probe(context)
        disableModules = preferences.disableModules
        softReboot = preferences.softReboot
        onPauseOrDispose { }
    }

    fun start() {
        if (running || !probe.supported || probe.rooted) return
        running = true
        log = ""
        outcome = ""
        notice = ""
        val exploit = ElevationExploit(context.applicationContext)
        val manager = probe.managerPackage
        scope.launch {
            try {
                // bootstrap.c parses these three keys straight out of the prefs
                // XML, so they have to be on disk before the payload starts.
                preferences.suManager = manager
                preferences.disableModules = disableModules
                preferences.softReboot = softReboot
                val code = withContext(Dispatchers.IO) {
                    val line = { text: String -> log = if (log.isEmpty()) text else "$log\n$text" }
                    exploit.reportDevice(manager, line)
                    val bootstrap = exploit.prepareBootstrap()
                    exploit.prepareDaemon(manager)
                    exploit.run(bootstrap, line)
                }
                outcome = when (code) {
                    0 -> resultStarted
                    1 -> resultBootstrap
                    2 -> resultTimeout
                    3 -> resultPatch
                    ElevationExploit.RESULT_ALREADY_HOOKED -> resultHooked
                    else -> resultFailed
                }
            } catch (error: Exception) {
                outcome = error.message ?: resultFailed
                log = if (log.isEmpty()) outcome else "$log\n$outcome"
            } finally {
                running = false
                probe = ElevationDevice.probe(context)
            }
        }
    }

    val blocked = when (probe.blocker) {
        ElevationBlocker.None -> ""
        ElevationBlocker.SecondaryUser -> blockedSecondaryUser
        ElevationBlocker.Abi -> blockedAbi
        ElevationBlocker.Kernel -> blockedKernel
        ElevationBlocker.Assets -> blockedAssets
        ElevationBlocker.Manager -> blockedManager
    }
    val statusText = notice.ifBlank { outcome }.ifBlank { blocked }
    val state = probe.toUiState(running, statusText, disableModules, softReboot, log)
    val actions = IkunElevationActions(
        onBack = { navigator.pop() },
        onRun = ::start,
        onRefresh = { probe = ElevationDevice.probe(context) },
        onCopyLog = {
            if (log.isNotBlank()) {
                context.getSystemService(ClipboardManager::class.java)
                    .setPrimaryClip(ClipData.newPlainText(label, log))
                notice = logCopied
            }
        },
        onSetDisableModules = { value ->
            disableModules = value
            preferences.disableModules = value
        },
        onSetSoftReboot = { value ->
            softReboot = value
            preferences.softReboot = value
        },
    )

    // Navigation already supplies the active theme, including its colors and blur settings.
    when (LocalUiMode.current) {
        UiMode.Miuix -> IkunElevationMiuix(state, actions)
        UiMode.Material -> IkunElevationMaterial(state, actions)
    }
}

private fun ElevationProbe.toUiState(
    running: Boolean,
    status: String,
    disableModules: Boolean,
    softReboot: Boolean,
    log: String,
) = IkunElevationUiState(
    supported = supported,
    rooted = rooted,
    running = running,
    status = status,
    packageName = managerPackage,
    kernel = kernelVersion,
    securityPatch = securityPatch,
    disableModules = disableModules,
    softReboot = softReboot,
    log = log,
)
