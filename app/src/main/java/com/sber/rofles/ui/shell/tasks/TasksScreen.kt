package com.sber.rofles.ui.shell.tasks

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sber.rofles.ui.shell.components.SkCard
import com.sber.rofles.ui.shell.components.SkGutter
import com.sber.rofles.ui.shell.components.SkPrimaryButton
import com.sber.rofles.ui.shell.components.SkScreenBackground
import com.sber.rofles.ui.shell.components.SkTabBarInset
import com.sber.rofles.ui.shell.components.SkTabTopBar
import com.sber.rofles.ui.theme.SberKids

private val SegmentSelected = Color(0xFFEBEFFF)
private val SegmentInactiveText = Color(0xFF8189BA)
private val CellGap = 16.dp

/** Вкладка «Дела»: интро-карточка, переключатель «Просто / Сложно» и список дел. */
@Composable
fun TasksScreen(
    onAvatarClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var level by rememberSaveable { mutableStateOf(TaskLevel.Hard) }
    val tasks = if (level == TaskLevel.Hard) HardTasks else EasyTasks

    SkScreenBackground(modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = SkTabBarInset),
        ) {
            item(key = "top") {
                SkTabTopBar(onAvatarClick = onAvatarClick)
            }
            item(key = "intro") {
                IntroCard(
                    onClick = {},
                    modifier = Modifier.padding(start = SkGutter, end = SkGutter, top = 24.dp),
                )
            }
            item(key = "title") {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                        .height(56.dp)
                        .padding(horizontal = SkGutter),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text("Чем заняться", style = SberKids.typography.headline, color = SberKids.colors.textPrimary)
                }
            }
            item(key = "segment") {
                LevelSegment(
                    selected = level,
                    onSelect = { level = it },
                    modifier = Modifier.padding(start = SkGutter, end = SkGutter, top = 8.dp, bottom = CellGap),
                )
            }
            items(tasks, key = { "${level.name}:${it.title}" }) { task ->
                TaskCell(
                    task = task,
                    onClick = {},
                    modifier = Modifier.padding(start = SkGutter, end = SkGutter, bottom = CellGap),
                )
            }
            item(key = "own") {
                SkPrimaryButton(
                    text = "Придумаю своё",
                    onClick = {},
                    modifier = Modifier.padding(start = SkGutter, end = SkGutter, top = 16.dp),
                )
            }
        }
    }
}

@Composable
private fun IntroCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = SberKids.colors
    SkCard(modifier.fillMaxWidth()) {
        Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 24.dp)) {
            Text(
                "Тут делаются дела",
                style = SberKids.typography.headline,
                color = colors.textPrimary,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Сообщи родителю, что готов выполнить полезные дела за вознаграждения",
                style = SberKids.typography.bodyRegular,
                color = colors.textSecondary,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
            Spacer(Modifier.height(24.dp))
            SkPrimaryButton(text = "Круто!", onClick = onClick)
        }
    }
}

@Composable
private fun LevelSegment(
    selected: TaskLevel,
    onSelect: (TaskLevel) -> Unit,
    modifier: Modifier = Modifier,
) {
    SkCard(modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TaskLevel.entries.forEach { level ->
                val isSelected = level == selected
                val bg by animateColorAsState(if (isSelected) SegmentSelected else Color.Transparent, label = "segBg")
                val fg by animateColorAsState(
                    if (isSelected) SberKids.colors.textPrimary else SegmentInactiveText,
                    label = "segFg",
                )
                Box(
                    Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(bg)
                        .clickable { onSelect(level) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        level.label,
                        style = SberKids.typography.body.copy(fontSize = 16.sp, fontWeight = FontWeight.Medium),
                        color = fg,
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskCell(task: TaskItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    SkCard(modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 96.dp)
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TaskIcon(task)
            Spacer(Modifier.width(9.dp))
            Text(
                task.title,
                style = SberKids.typography.body.copy(fontSize = 16.sp, lineHeight = 20.sp),
                color = SberKids.colors.textPrimary,
                modifier = Modifier.weight(1f).padding(end = 16.dp),
            )
        }
    }
}

/** Иконка дела: цветной «скруглённый квадрат» 48 в модуле 56 и иконка 24. */
@Composable
private fun TaskIcon(task: TaskItem) {
    val shape = RoundedCornerShape(20.dp)
    Box(Modifier.size(56.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(48.dp)
                .clip(shape)
                .background(task.tone.background),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(task.icon),
                contentDescription = null,
                tint = task.tone.icon,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
