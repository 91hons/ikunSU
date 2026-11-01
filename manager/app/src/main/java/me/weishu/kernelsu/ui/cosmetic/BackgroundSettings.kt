package me.weishu.kernelsu.ui.cosmetic

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.weishu.kernelsu.R
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Slider as MiuixSlider
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.math.roundToInt

/**
 * Background appearance + theme package controls.
 *
 * Two flavours so each UI mode keeps its native look: [BackgroundSettingsCardMiuix]
 * uses Miuix rows/cards, [BackgroundSettingsCardMaterial] uses Material 3 ones.
 * Both read and mutate [BackgroundStore] directly.
 */
private class BackgroundHandles(
    val pick: () -> Unit,
    val export: () -> Unit,
    val importTheme: () -> Unit,
    val useWallpaper: () -> Unit,
    val clear: () -> Unit,
    val reset: () -> Unit,
    val setAlpha: (Float) -> Unit,
    val setBlur: (Float) -> Unit,
    val setDim: (Float) -> Unit,
)

@Composable
private fun rememberBackgroundHandles(): Pair<BackgroundState, BackgroundHandles> {
    val context = LocalContext.current
    val state by BackgroundStore.state.collectAsState()
    val pick = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) BackgroundStore.setUri(context, uri)
    }
    val export = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip"),
    ) { uri -> if (uri != null) ThemePackage.export(context, uri) }
    val importTheme = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) ThemePackage.import(context, uri)
    }
    val handles = BackgroundHandles(
        pick = { pick.launch(arrayOf("image/*")) },
        export = { export.launch("ikunSU-theme.zip") },
        importTheme = { importTheme.launch(arrayOf("application/zip", "application/octet-stream")) },
        useWallpaper = { BackgroundStore.useSystemWallpaper(context) },
        clear = { BackgroundStore.setUri(context, null) },
        reset = { BackgroundStore.reset(context) },
        setAlpha = { BackgroundStore.setAlpha(context, it) },
        setBlur = { BackgroundStore.setBlur(context, it) },
        setDim = { BackgroundStore.setDim(context, it) },
    )
    return state to handles
}

/* ------------------------------------------------------------- Miuix */

@Composable
fun BackgroundSettingsCardMiuix(modifier: Modifier = Modifier) {
    val (state, handles) = rememberBackgroundHandles()
    var expanded by remember { mutableStateOf(true) }
    val colorScheme = MiuixTheme.colorScheme

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(Modifier.fillMaxWidth()) {
            SwitchPreference(
                title = stringResource(R.string.ikun_background_wallpaper),
                summary = stringResource(R.string.ikun_background_wallpaper_summary),
                checked = state.enabled,
                onCheckedChange = { on -> if (on) handles.useWallpaper() else handles.clear() },
            )
            BasicComponent(
                title = stringResource(R.string.ikun_background_pick),
                summary = stringResource(
                    if (state.enabled) R.string.ikun_background_set else R.string.ikun_background_unset,
                ),
                onClick = handles.pick,
            )
            BasicComponent(
                title = stringResource(R.string.ikun_background_appearance),
                summary = stringResource(R.string.ikun_background_appearance_summary),
                onClick = { expanded = !expanded },
                endActions = {
                    androidx.compose.material3.Icon(
                        imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = null,
                        tint = colorScheme.onSurfaceVariantActions,
                    )
                },
            )
            if (expanded) {
                BasicComponent(
                    title = stringResource(R.string.ikun_background_alpha),
                    endActions = {
                        Text(
                            "${(state.alpha * 100).roundToInt()}%",
                            color = colorScheme.onSurfaceVariantActions,
                        )
                    },
                    bottomAction = {
                        MiuixSlider(
                            value = state.alpha,
                            onValueChange = handles.setAlpha,
                            valueRange = 0.1f..1f,
                        )
                    },
                )
                BasicComponent(
                    title = stringResource(R.string.ikun_background_blur),
                    endActions = {
                        Text(
                            "${state.blur.roundToInt()}dp",
                            color = colorScheme.onSurfaceVariantActions,
                        )
                    },
                    bottomAction = {
                        MiuixSlider(
                            value = state.blur,
                            onValueChange = handles.setBlur,
                            valueRange = 0f..25f,
                        )
                    },
                )
                BasicComponent(
                    title = stringResource(R.string.ikun_background_dim),
                    endActions = {
                        Text(
                            "${(state.dim * 100).roundToInt()}%",
                            color = colorScheme.onSurfaceVariantActions,
                        )
                    },
                    bottomAction = {
                        MiuixSlider(
                            value = state.dim,
                            onValueChange = handles.setDim,
                            valueRange = 0f..0.7f,
                        )
                    },
                )
            }
        }
        Card(Modifier.fillMaxWidth()) {
            BasicComponent(
                title = stringResource(R.string.ikun_theme_export),
                summary = stringResource(R.string.ikun_theme_export_summary),
                onClick = handles.export,
            )
            BasicComponent(
                title = stringResource(R.string.ikun_theme_import),
                summary = stringResource(R.string.ikun_theme_import_summary),
                onClick = handles.importTheme,
            )
            BasicComponent(
                title = stringResource(R.string.ikun_theme_reset),
                summary = stringResource(R.string.ikun_theme_reset_summary),
                onClick = handles.reset,
            )
        }
    }
}

/* ---------------------------------------------------------- Material */

@Composable
fun BackgroundSettingsCardMaterial(modifier: Modifier = Modifier) {
    val (state, handles) = rememberBackgroundHandles()
    var expanded by remember { mutableStateOf(true) }

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SwitchRow(
            icon = Icons.Filled.Wallpaper,
            title = stringResource(R.string.ikun_background_wallpaper),
            summary = stringResource(R.string.ikun_background_wallpaper_summary),
            checked = state.enabled,
            onCheckedChange = { on -> if (on) handles.useWallpaper() else handles.clear() },
        )
        ActionRow(
            icon = Icons.Filled.UploadFile,
            title = stringResource(R.string.ikun_background_pick),
            summary = stringResource(
                if (state.enabled) R.string.ikun_background_set else R.string.ikun_background_unset,
            ),
            onClick = handles.pick,
        )
        ActionRow(
            icon = Icons.Filled.Build,
            title = stringResource(R.string.ikun_background_appearance),
            summary = stringResource(R.string.ikun_background_appearance_summary),
            onClick = { expanded = !expanded },
            trailing = {
                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
        )
        if (expanded) {
            Column(Modifier.padding(horizontal = 16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.ikun_background_alpha), style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "${(state.alpha * 100).roundToInt()}%",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Slider(value = state.alpha, onValueChange = handles.setAlpha, valueRange = 0.1f..1f)
            }
        }
        ActionRow(
            icon = Icons.Filled.SaveAlt,
            title = stringResource(R.string.ikun_theme_export),
            summary = stringResource(R.string.ikun_theme_export_summary),
            onClick = handles.export,
        )
        ActionRow(
            icon = Icons.Filled.UploadFile,
            title = stringResource(R.string.ikun_theme_import),
            summary = stringResource(R.string.ikun_theme_import_summary),
            onClick = handles.importTheme,
        )
        ActionRow(
            icon = Icons.Filled.Restore,
            title = stringResource(R.string.ikun_theme_reset),
            summary = stringResource(R.string.ikun_theme_reset_summary),
            onClick = handles.reset,
        )
    }
}

@Composable
private fun SwitchRow(
    icon: ImageVector,
    title: String,
    summary: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ActionRow(
    icon: ImageVector,
    title: String,
    summary: String,
    onClick: () -> Unit,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        trailing?.invoke()
    }
}
