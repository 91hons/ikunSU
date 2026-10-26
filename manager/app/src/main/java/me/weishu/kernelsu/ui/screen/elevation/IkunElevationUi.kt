package me.weishu.kernelsu.ui.screen.elevation

import androidx.annotation.StringRes
import me.weishu.kernelsu.R

internal data class IkunElevationUiState(
    val supported: Boolean,
    val rooted: Boolean,
    val running: Boolean,
    val status: String,
    val packageName: String,
    val kernel: String,
    val securityPatch: String,
    val disableModules: Boolean,
    val softReboot: Boolean,
    val log: String,
)

internal data class IkunElevationActions(
    val onBack: () -> Unit,
    val onRun: () -> Unit,
    val onRefresh: () -> Unit,
    val onCopyLog: () -> Unit,
    val onSetDisableModules: (Boolean) -> Unit,
    val onSetSoftReboot: (Boolean) -> Unit,
)

internal val IkunElevationUiState.canStart: Boolean
    get() = supported && !rooted && !running

internal val IkunElevationUiState.actionLabel: Int
    @StringRes get() = when {
        running -> R.string.ikun_elevation_running
        rooted -> R.string.ikun_elevation_rooted
        !supported -> R.string.ikun_elevation_unsupported
        else -> R.string.ikun_elevation_start
    }

internal val IkunElevationUiState.statusLabel: Int
    @StringRes get() = when {
        running -> R.string.ikun_elevation_running
        rooted -> R.string.ikun_elevation_rooted
        !supported -> R.string.ikun_elevation_unsupported
        else -> R.string.ikun_elevation_supported
    }
