package com.sber.rofles.ui.shell.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sber.rofles.R
import com.sber.rofles.ui.theme.SberKids

val SkGutter = 16.dp

val SkCellGap = 2.dp

@Composable
fun SkScreenBackground(
    modifier: Modifier = Modifier,
    blobs: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = SberKids.colors
    Box(
        modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        if (blobs) {
            Blob(colors.blobPink, size = 360.dp, x = (-150).dp, y = 20.dp)
            Blob(colors.blobYellow, size = 420.dp, x = 40.dp, y = 60.dp)
            Blob(colors.blobPink, size = 360.dp, x = 260.dp, y = 180.dp)
        }
        content()
        Box(
            Modifier
                .fillMaxWidth()
                .windowInsetsTopHeight(WindowInsets.statusBars)
                .background(colors.background.copy(alpha = 0.92f)),
        )
    }
}

@Composable
private fun Blob(color: Color, size: Dp, x: Dp, y: Dp) {
    Box(
        Modifier
            .offset(x, y)
            .size(size)
            .background(
                Brush.radialGradient(
                    0f to color.copy(alpha = 0.42f),
                    0.45f to color.copy(alpha = 0.22f),
                    1f to Color.Transparent,
                ),
            ),
    )
}

fun Modifier.skClickable(onClick: (() -> Unit)?): Modifier =
    if (onClick == null) this else this.clickable(onClick = onClick)

@Composable
fun SkCard(
    modifier: Modifier = Modifier,
    shape: Shape = SberKids.shapes.card,
    color: Color = SberKids.colors.surface,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier
            .clip(shape)
            .background(color)
            .skClickable(onClick),
        content = content,
    )
}

fun skGroupShape(index: Int, count: Int): Shape {
    val big = 20.dp
    val small = 4.dp
    val top = if (index == 0) big else small
    val bottom = if (index == count - 1) big else small
    return RoundedCornerShape(topStart = top, topEnd = top, bottomStart = bottom, bottomEnd = bottom)
}

@Composable
fun SkCellGroup(
    modifier: Modifier = Modifier,
    items: List<@Composable (Shape) -> Unit>,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(SkCellGap)) {
        items.forEachIndexed { index, item -> item(skGroupShape(index, items.size)) }
    }
}

@Composable
fun SkCell(
    title: String,
    modifier: Modifier = Modifier,
    shape: Shape = SberKids.shapes.card,
    @DrawableRes icon: Int? = null,
    iconTint: Color? = SberKids.colors.iconMono,
    leading: (@Composable () -> Unit)? = null,
    subtitle: String? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    val colors = SberKids.colors
    val type = SberKids.typography
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .clip(shape)
            .background(colors.surface)
            .skClickable(onClick)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when {
            leading != null -> {
                leading()
                Spacer(Modifier.width(16.dp))
            }
            icon != null -> {
                Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                    if (iconTint != null) {
                        Icon(painterResource(icon), contentDescription = null, tint = iconTint)
                    } else {
                        Image(painterResource(icon), contentDescription = null)
                    }
                }
                Spacer(Modifier.width(16.dp))
            }
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = type.body, color = colors.textPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(subtitle, style = type.footnote, color = colors.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(12.dp))
            trailing()
        }
    }
}

@Composable
fun SkIconButton(
    @DrawableRes icon: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color? = SberKids.colors.iconMono,
    contentDescription: String? = null,
) {
    Box(
        modifier
            .size(56.dp)
            .shadow(12.dp, SberKids.shapes.button, ambientColor = Color(0x14000000), spotColor = Color(0x14000000))
            .clip(SberKids.shapes.button)
            .background(SberKids.colors.surface)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (tint != null) {
            Icon(painterResource(icon), contentDescription = contentDescription, tint = tint)
        } else {
            Image(painterResource(icon), contentDescription = contentDescription)
        }
    }
}

@Composable
fun SkAvatar(
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    @DrawableRes image: Int = R.drawable.sk_avatar_monkey,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
) {
    val clickModifier = if (onClick == null && onLongClick == null) {
        Modifier
    } else {
        Modifier.combinedClickable(onClick = { onClick?.invoke() }, onLongClick = onLongClick)
    }
    Image(
        painter = painterResource(image),
        contentDescription = "Аватар",
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .then(clickModifier),
    )
}

data class SkMenuItem(val title: String, val onClick: () -> Unit)

@Composable
fun SkTabTopBar(
    modifier: Modifier = Modifier,
    title: String? = null,
    onAvatarClick: (() -> Unit)? = null,
    avatarMenu: List<SkMenuItem> = emptyList(),
    actions: @Composable RowScope.() -> Unit = {},
) {
    var menuOpen by remember { mutableStateOf(false) }
    Row(
        modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box {
            SkAvatar(
                onClick = onAvatarClick,
                onLongClick = if (avatarMenu.isEmpty()) null else ({ menuOpen = true }),
            )
            DropdownMenu(
                expanded = menuOpen,
                onDismissRequest = { menuOpen = false },
                shape = SberKids.shapes.card,
                containerColor = SberKids.colors.surface,
            ) {
                avatarMenu.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item.title, style = SberKids.typography.body, color = SberKids.colors.textPrimary) },
                        onClick = {
                            menuOpen = false
                            item.onClick()
                        },
                    )
                }
            }
        }
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            if (title != null) {
                Text(title, style = SberKids.typography.navTitle, color = SberKids.colors.textPrimary)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), content = actions)
    }
}

@Composable
fun SkBackTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    Box(
        modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        SkIconButton(
            icon = R.drawable.sk_ic_back,
            onClick = onBack,
            contentDescription = "Назад",
            modifier = Modifier.align(Alignment.CenterStart),
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 64.dp)) {
            Text(
                title,
                style = SberKids.typography.navTitle,
                color = SberKids.colors.textPrimary,
                textAlign = TextAlign.Center,
            )
            if (subtitle != null) {
                Text(subtitle, style = SberKids.typography.footnote, color = SberKids.colors.textTertiary)
            }
        }
    }
}

@Composable
fun SkPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    brush: Brush = SberKids.gradients.primaryButton,
    @DrawableRes icon: Int? = null,
    textColor: Color = SberKids.colors.textPrimary,
) {
    Row(
        modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(SberKids.shapes.button)
            .background(brush)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(painterResource(icon), contentDescription = null, tint = textColor, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
        }
        Text(text, style = SberKids.typography.button, color = textColor)
    }
}

@Composable
fun SkLimeButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    SkPrimaryButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        brush = Brush.linearGradient(listOf(SberKids.colors.lime, SberKids.colors.lime)),
    )
}

@Composable
fun SkSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SberKids.colors
    val track by animateColorAsState(if (checked) colors.toggleOn else colors.toggleOff, label = "track")
    val thumbX by animateDpAsState(if (checked) 18.dp else 2.dp, label = "thumb")
    Box(
        modifier
            .size(width = 40.dp, height = 24.dp)
            .clip(CircleShape)
            .background(track)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false),
            ) { onCheckedChange(!checked) },
    ) {
        Box(
            Modifier
                .offset(x = thumbX, y = 2.dp)
                .size(20.dp)
                .shadow(2.dp, CircleShape)
                .background(Color.White, CircleShape),
        )
    }
}

@Composable
fun SkProgressBar(progress: Float, modifier: Modifier = Modifier, color: Color = SberKids.colors.green) {
    Box(
        modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(CircleShape)
            .background(SberKids.colors.progressTrack),
    ) {
        Box(
            Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(6.dp)
                .clip(CircleShape)
                .background(color),
        )
    }
}

@Composable
fun SkSectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = SberKids.typography.headline,
        color = SberKids.colors.textPrimary,
        modifier = modifier.padding(horizontal = SkGutter),
    )
}

@Composable
fun SkMoneyText(
    rubles: String,
    kopecks: String?,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = SberKids.typography.balance,
) {
    val colors = SberKids.colors
    Text(
        text = androidx.compose.ui.text.buildAnnotatedString {
            pushStyle(androidx.compose.ui.text.SpanStyle(color = colors.textPrimary))
            append(rubles)
            pop()
            pushStyle(androidx.compose.ui.text.SpanStyle(color = colors.textSecondary))
            if (kopecks != null) append(",$kopecks")
            append(" ₽")
            pop()
        },
        style = style,
        modifier = modifier,
    )
}

val SkTabBarInset: Dp
    @Composable get() = 128.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

@Composable
fun SkGutterColumn(
    modifier: Modifier = Modifier,
    spacing: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier.padding(horizontal = SkGutter),
        verticalArrangement = Arrangement.spacedBy(spacing),
        content = content,
    )
}

@Composable
fun SkCenteredText(text: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, style = SberKids.typography.body, color = SberKids.colors.textTertiary)
    }
}
