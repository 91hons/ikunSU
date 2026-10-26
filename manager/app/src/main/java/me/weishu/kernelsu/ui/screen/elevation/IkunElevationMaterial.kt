package me.weishu.kernelsu.ui.screen.elevation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.component.material.ExpressiveScaffold
import me.weishu.kernelsu.ui.component.material.ExpressiveSwitch
import me.weishu.kernelsu.ui.component.material.TonalCard
import me.weishu.kernelsu.ui.component.material.TopBarBackButton
import me.weishu.kernelsu.ui.component.material.expressiveTopAppBarColors

@Composable
internal fun IkunElevationMaterial(state: IkunElevationUiState, actions: IkunElevationActions) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    ExpressiveScaffold(
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(stringResource(R.string.ikun_elevation_title)) },
                navigationIcon = { TopBarBackButton(onClick = actions.onBack) },
                actions = {
                    IconButton(onClick = actions.onRefresh, enabled = !state.running) {
                        Icon(Icons.Rounded.Refresh, stringResource(R.string.ikun_elevation_refresh))
                    }
                },
                colors = expressiveTopAppBarColors(),
                windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
                scrollBehavior = scrollBehavior,
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 13.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            item {
                TonalCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            stringResource(state.statusLabel),
                            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        if (state.status.isNotBlank()) {
                            Text(state.status, style = MaterialTheme.typography.bodyMedium)
                        }
                        Text(
                            stringResource(
                                when {
                                    state.rooted -> R.string.ikun_elevation_rooted_summary
                                    state.supported -> R.string.ikun_elevation_summary
                                    else -> R.string.ikun_elevation_unsupported_summary
                                },
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Button(
                            onClick = actions.onRun,
                            enabled = state.canStart,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        ) {
                            Text(stringResource(state.actionLabel))
                        }
                    }
                }
            }
            item {
                TonalCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(stringResource(R.string.ikun_elevation_device_title), style = MaterialTheme.typography.titleMedium)
                        DeviceField(stringResource(R.string.ikun_elevation_package), state.packageName)
                        DeviceField(stringResource(R.string.ikun_elevation_kernel), state.kernel)
                        DeviceField(stringResource(R.string.ikun_elevation_security_patch), state.securityPatch)
                    }
                }
            }
            item {
                TonalCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                        Text(stringResource(R.string.ikun_elevation_options_title), style = MaterialTheme.typography.titleMedium)
                        Option(
                            title = stringResource(R.string.ikun_elevation_disable_modules),
                            summary = stringResource(R.string.ikun_elevation_disable_modules_summary),
                            checked = state.disableModules,
                            enabled = !state.running,
                            onCheckedChange = actions.onSetDisableModules,
                        )
                        Option(
                            title = stringResource(R.string.ikun_elevation_soft_reboot),
                            summary = stringResource(R.string.ikun_elevation_soft_reboot_summary),
                            checked = state.softReboot,
                            enabled = !state.running,
                            onCheckedChange = actions.onSetSoftReboot,
                        )
                    }
                }
            }
            item {
                TonalCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(stringResource(R.string.ikun_elevation_log_title), style = MaterialTheme.typography.titleMedium)
                        SelectionContainer {
                            Text(
                                text = state.log.ifBlank { stringResource(R.string.ikun_elevation_log_empty) },
                                modifier = Modifier.fillMaxWidth(),
                                fontFamily = FontFamily.Monospace,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        FilledTonalButton(
                            onClick = actions.onCopyLog,
                            enabled = state.log.isNotBlank(),
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        ) {
                            Text(stringResource(R.string.ikun_elevation_copy_log))
                        }
                    }
                }
            }
            item {
                Text(
                    stringResource(R.string.ikun_elevation_attribution),
                    modifier = Modifier.padding(horizontal = 4.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item {
                Spacer(Modifier.height(WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()))
            }
        }
    }
}

@Composable
private fun DeviceField(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        SelectionContainer {
            Text(
                value.ifBlank { stringResource(R.string.ikun_elevation_unknown) },
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun Option(title: String, summary: String, checked: Boolean, enabled: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        ExpressiveSwitch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}
