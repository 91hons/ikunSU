package me.weishu.kernelsu.ui.cosmetic

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.weishu.kernelsu.R
import kotlin.math.roundToInt

/**
 * Background and theme package controls.
 *
 * Self contained on purpose: it reads [BackgroundStore] directly and mutates it,
 * so it can be dropped into any settings screen regardless of that screen's own
 * state holder.
 */
@Composable
fun BackgroundSettingsCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val state by BackgroundStore.state.collectAsState()

    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) BackgroundStore.setUri(context, uri)
    }
    val exportTheme = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        if (uri != null) ThemePackage.export(context, uri)
    }
    val importTheme = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) ThemePackage.import(context, uri)
    }

    Column(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stringResource(R.string.ikun_background), style = MaterialTheme.typography.titleMedium)
        Text(
            stringResource(R.string.ikun_background_summary),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
        Text(
            stringResource(if (state.enabled) R.string.ikun_background_set else R.string.ikun_background_unset),
            style = MaterialTheme.typography.bodyMedium,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FilledTonalButton(onClick = { pickImage.launch(arrayOf("image/*")) }, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.ikun_background_pick))
            }
            if (state.enabled) {
                Button(onClick = { BackgroundStore.setUri(context, null) }, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.ikun_background_clear))
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FilledTonalButton(
                onClick = { BackgroundStore.useSystemWallpaper(context) },
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.ikun_background_wallpaper))
            }
            Button(onClick = { BackgroundStore.reset(context) }, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.ikun_theme_reset))
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FilledTonalButton(onClick = { exportTheme.launch("ikunSU-theme.zip") }, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.ikun_theme_export))
            }
            Button(
                onClick = { importTheme.launch(arrayOf("application/zip", "application/octet-stream")) },
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.ikun_theme_import))
            }
        }

        if (state.enabled) {
            SliderRow(
                title = stringResource(R.string.ikun_background_alpha),
                label = "${(state.alpha * 100).roundToInt()}%",
                value = state.alpha,
                range = 0.1f..1f,
                onValueChange = { BackgroundStore.setAlpha(context, it) },
            )
            SliderRow(
                title = stringResource(R.string.ikun_background_blur),
                label = "${state.blur.roundToInt()}dp",
                value = state.blur,
                range = 0f..25f,
                onValueChange = { BackgroundStore.setBlur(context, it) },
            )
            SliderRow(
                title = stringResource(R.string.ikun_background_dim),
                label = "${(state.dim * 100).roundToInt()}%",
                value = state.dim,
                range = 0f..0.7f,
                onValueChange = { BackgroundStore.setDim(context, it) },
            )
        }
    }
}

@Composable
private fun SliderRow(
    title: String,
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
        Slider(value = value, onValueChange = onValueChange, valueRange = range)
    }
}
