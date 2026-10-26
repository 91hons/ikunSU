package me.weishu.kernelsu.ui.screen.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.component.material.TonalCard
import top.yukonga.miuix.kmp.basic.Button as MiuixButton
import top.yukonga.miuix.kmp.basic.ButtonDefaults as MiuixButtonDefaults
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun IkunIntroCardMiuix(modifier: Modifier = Modifier) {
    val colors = MiuixTheme.colorScheme
    val textStyles = MiuixTheme.textStyles
    MiuixCard(modifier = modifier.fillMaxWidth()) {
        IkunIntroContent(
            contentColor = colors.onSurface,
            secondaryColor = colors.onSurfaceVariantSummary,
            quoteColor = colors.surface,
            avatarColor = colors.secondaryContainer,
            titleStyle = textStyles.headline1.copy(fontSize = 22.sp, lineHeight = 30.sp, fontWeight = FontWeight.SemiBold),
            bodyStyle = textStyles.body2,
            quoteStyle = textStyles.body1.copy(fontSize = 20.sp, lineHeight = 30.sp),
            captionStyle = textStyles.footnote1,
        ) { onRefresh ->
            MiuixButton(
                onClick = onRefresh,
                modifier = Modifier.heightIn(min = 48.dp),
                colors = MiuixButtonDefaults.buttonColors(
                    color = colors.primary.copy(alpha = 0.12f),
                    contentColor = colors.primary,
                ),
            ) {
                QuoteRefreshLabel(color = colors.primary, style = textStyles.button)
            }
        }
    }
}

@Composable
internal fun IkunIntroCardMaterial(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    TonalCard(modifier = modifier.fillMaxWidth()) {
        IkunIntroContent(
            contentColor = colors.onSurface,
            secondaryColor = colors.onSurfaceVariant,
            quoteColor = colors.surfaceContainerLow,
            avatarColor = colors.secondaryContainer,
            titleStyle = typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            bodyStyle = typography.bodyMedium,
            quoteStyle = typography.titleLarge.copy(lineHeight = 30.sp),
            captionStyle = typography.bodySmall,
        ) { onRefresh ->
            FilledTonalButton(
                onClick = onRefresh,
                modifier = Modifier.heightIn(min = 48.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = colors.secondaryContainer,
                    contentColor = colors.onSecondaryContainer,
                ),
            ) {
                QuoteRefreshLabel(color = colors.onSecondaryContainer, style = typography.labelLarge)
            }
        }
    }
}

@Composable
private fun IkunIntroContent(
    contentColor: Color,
    secondaryColor: Color,
    quoteColor: Color,
    avatarColor: Color,
    titleStyle: TextStyle,
    bodyStyle: TextStyle,
    quoteStyle: TextStyle,
    captionStyle: TextStyle,
    refreshButton: @Composable (() -> Unit) -> Unit,
) {
    val quotes = stringArrayResource(R.array.ikun_home_quotes)
    var quoteIndex by rememberSaveable { mutableIntStateOf(0) }
    Column(
        modifier = Modifier.fillMaxWidth().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val stackHeader = maxWidth < 280.dp * LocalDensity.current.fontScale
            val introduction = @Composable {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(R.string.ikun_home_greeting),
                        color = contentColor,
                        style = titleStyle,
                    )
                    Text(
                        text = stringResource(R.string.ikun_home_introduction),
                        color = secondaryColor,
                        style = bodyStyle,
                    )
                }
            }
            if (stackHeader) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    IkunAvatar(avatarColor)
                    introduction()
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    IkunAvatar(avatarColor)
                    Column(modifier = Modifier.weight(1f)) { introduction() }
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(quoteColor)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(R.string.ikun_home_quote_label),
                color = secondaryColor,
                style = captionStyle,
            )
            Text(
                text = quotes[quoteIndex.mod(quotes.size)],
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                color = contentColor,
                style = quoteStyle,
            )
        }
        refreshButton { quoteIndex = (quoteIndex + 1).mod(quotes.size) }
        Text(
            text = stringResource(R.string.ikun_home_attribution),
            color = secondaryColor,
            style = captionStyle,
        )
    }
}

@Composable
private fun IkunAvatar(backgroundColor: Color) {
    Image(
        painter = painterResource(R.drawable.ikun_launcher),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.size(60.dp).clip(CircleShape).background(backgroundColor),
    )
}

@Composable
private fun RowScope.QuoteRefreshLabel(color: Color, style: TextStyle) {
    Icon(imageVector = Icons.Rounded.Refresh, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
    Spacer(Modifier.width(8.dp))
    Text(
        text = stringResource(R.string.ikun_home_refresh_quote),
        modifier = Modifier.weight(1f, fill = false),
        color = color,
        style = style,
    )
}
