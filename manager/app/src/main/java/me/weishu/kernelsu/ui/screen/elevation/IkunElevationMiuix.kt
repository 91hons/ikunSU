package me.weishu.kernelsu.ui.screen.elevation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.theme.LocalEnableBlur
import me.weishu.kernelsu.ui.util.BlurredBar
import me.weishu.kernelsu.ui.util.rememberBlurBackdrop
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

@Composable
internal fun IkunElevationMiuix(state: IkunElevationUiState, actions: IkunElevationActions) {
    val scrollBehavior = MiuixScrollBehavior()
    val backdrop = rememberBlurBackdrop(LocalEnableBlur.current)
    val barColor = if (backdrop != null) Color.Transparent else colorScheme.surface
    Scaffold(
        topBar = {
            BlurredBar(backdrop) {
                TopAppBar(
                    color = barColor,
                    title = stringResource(R.string.ikun_elevation_title),
                    navigationIcon = {
                        IconButton(onClick = actions.onBack) {
                            val direction = LocalLayoutDirection.current
                            Icon(
                                imageVector = MiuixIcons.Back,
                                contentDescription = null,
                                tint = colorScheme.onBackground,
                                modifier = Modifier.graphicsLayer {
                                    if (direction == LayoutDirection.Rtl) scaleX = -1f
                                },
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = actions.onRefresh, enabled = !state.running) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = stringResource(R.string.ikun_elevation_refresh),
                                tint = colorScheme.onBackground,
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior,
                )
            }
        },
        popupHost = { },
        contentWindowInsets = WindowInsets.systemBars.add(WindowInsets.displayCutout)
            .only(WindowInsetsSides.Horizontal),
    ) { innerPadding ->
        Box(modifier = if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier) {
            LazyColumn(
                modifier = Modifier.fillMaxHeight().scrollEndHaptic().overScrollVertical()
                    .nestedScroll(scrollBehavior.nestedScrollConnection).padding(horizontal = 12.dp),
                contentPadding = innerPadding,
                overscrollEffect = null,
            ) {
                item {
                    Card(Modifier.padding(top = 12.dp).fillMaxWidth()) {
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text(
                                stringResource(state.statusLabel),
                                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                                style = MiuixTheme.textStyles.headline1,
                                color = colorScheme.primary,
                            )
                            if (state.status.isNotBlank()) {
                                Text(state.status, style = MiuixTheme.textStyles.body2)
                            }
                            Text(
                                stringResource(
                                    when {
                                        state.rooted -> R.string.ikun_elevation_rooted_summary
                                        state.supported -> R.string.ikun_elevation_summary
                                        else -> R.string.ikun_elevation_unsupported_summary
                                    },
                                ),
                                style = MiuixTheme.textStyles.body2,
                                color = colorScheme.onSurfaceVariantSummary,
                            )
                            TextButton(
                                text = stringResource(state.actionLabel),
                                onClick = actions.onRun,
                                enabled = state.canStart,
                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                                colors = ButtonDefaults.textButtonColorsPrimary(),
                            )
                        }
                    }
                }
                item {
                    Card(Modifier.padding(top = 12.dp).fillMaxWidth()) {
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(stringResource(R.string.ikun_elevation_device_title), style = MiuixTheme.textStyles.body1.copy(fontWeight = FontWeight.SemiBold))
                            DeviceField(stringResource(R.string.ikun_elevation_package), state.packageName)
                            DeviceField(stringResource(R.string.ikun_elevation_kernel), state.kernel)
                            DeviceField(stringResource(R.string.ikun_elevation_security_patch), state.securityPatch)
                        }
                    }
                }
                item {
                    Card(Modifier.padding(top = 12.dp).fillMaxWidth()) {
                        Text(
                            stringResource(R.string.ikun_elevation_options_title),
                            modifier = Modifier.padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 4.dp),
                            style = MiuixTheme.textStyles.body1.copy(fontWeight = FontWeight.SemiBold),
                        )
                        SwitchPreference(
                            title = stringResource(R.string.ikun_elevation_disable_modules),
                            summary = stringResource(R.string.ikun_elevation_disable_modules_summary),
                            checked = state.disableModules,
                            enabled = !state.running,
                            onCheckedChange = actions.onSetDisableModules,
                        )
                        SwitchPreference(
                            title = stringResource(R.string.ikun_elevation_soft_reboot),
                            summary = stringResource(R.string.ikun_elevation_soft_reboot_summary),
                            checked = state.softReboot,
                            enabled = !state.running,
                            onCheckedChange = actions.onSetSoftReboot,
                        )
                    }
                }
                item {
                    Card(Modifier.padding(top = 12.dp).fillMaxWidth()) {
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text(stringResource(R.string.ikun_elevation_log_title), style = MiuixTheme.textStyles.body1.copy(fontWeight = FontWeight.SemiBold))
                            SelectionContainer {
                                Text(
                                    text = state.log.ifBlank { stringResource(R.string.ikun_elevation_log_empty) },
                                    modifier = Modifier.fillMaxWidth(),
                                    fontFamily = FontFamily.Monospace,
                                    style = MiuixTheme.textStyles.footnote1,
                                )
                            }
                            TextButton(
                                text = stringResource(R.string.ikun_elevation_copy_log),
                                onClick = actions.onCopyLog,
                                enabled = state.log.isNotBlank(),
                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                            )
                        }
                    }
                }
                item {
                    Text(
                        stringResource(R.string.ikun_elevation_attribution),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 16.dp),
                        color = colorScheme.onSurfaceVariantSummary,
                        style = MiuixTheme.textStyles.footnote1,
                    )
                }
                item {
                    Spacer(Modifier.height(WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 12.dp))
                }
            }
        }
    }
}

@Composable
private fun DeviceField(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MiuixTheme.textStyles.footnote1, color = colorScheme.onSurfaceVariantSummary)
        SelectionContainer {
            Text(
                value.ifBlank { stringResource(R.string.ikun_elevation_unknown) },
                style = MiuixTheme.textStyles.body2,
            )
        }
    }
}
