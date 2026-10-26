package me.weishu.kernelsu.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.component.material.SegmentedColumn
import me.weishu.kernelsu.ui.component.material.SegmentedListItem
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.basic.Icon as MiuixIcon

/**
 * Entry point for ikun提权, shared by the home page and the install page.
 *
 * Both are reachable before Root exists, which is the whole point: the bottom bar
 * and the settings page only appear once the kernel side is up, so an unrooted
 * phone has to be able to reach the payload from here instead.
 */
@Composable
fun IkunElevationEntryMaterial(onOpen: () -> Unit, modifier: Modifier = Modifier) {
    SegmentedColumn(modifier = modifier) {
        item {
            SegmentedListItem(
                onClick = onOpen,
                headlineContent = { Text(stringResource(R.string.ikun_elevation_title)) },
                supportingContent = { Text(stringResource(R.string.ikun_elevation_entry_summary)) },
                leadingContent = { Icon(Icons.Rounded.Security, null) },
                trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null) },
            )
        }
    }
}

@Composable
fun IkunElevationEntryMiuix(onOpen: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        ArrowPreference(
            title = stringResource(R.string.ikun_elevation_title),
            summary = stringResource(R.string.ikun_elevation_entry_summary),
            startAction = {
                MiuixIcon(
                    Icons.Rounded.Security,
                    modifier = Modifier.padding(end = 6.dp),
                    contentDescription = stringResource(R.string.ikun_elevation_title),
                    tint = colorScheme.onBackground,
                )
            },
            onClick = onOpen,
        )
    }
}
