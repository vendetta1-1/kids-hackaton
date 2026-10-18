package com.sber.rofles.ui.shell.tasks

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.sber.rofles.R

internal enum class TaskTone(val background: Color, val icon: Color) {
    Yellow(Color(0xFFFFF5C2), Color(0xFFFFC300)),
    Orange(Color(0xFFFFEAE0), Color(0xFFFF8E00)),
    Pink(Color(0xFFFFE0EC), Color(0xFFFF7EB0)),
    Blue(Color(0xFFCFECFF), Color(0xFF52BAFF)),
    Green(Color(0xFFBEFFEB), Color(0xFF3AE6A1)),
}

internal data class TaskItem(
    val title: String,
    @DrawableRes val icon: Int,
    val tone: TaskTone,
)

internal enum class TaskLevel(val label: String) { Easy("Просто"), Hard("Сложно") }

internal val HardTasks = listOf(
    TaskItem("Погуляю с собакой", R.drawable.tasks_ic_paw, TaskTone.Yellow),
    TaskItem("Уберусь в комнате", R.drawable.tasks_ic_home, TaskTone.Orange),
    TaskItem("Присмотрю за младшими", R.drawable.tasks_ic_carriage, TaskTone.Green),
    TaskItem("Придумаю план дел на неделю", R.drawable.tasks_ic_calendar, TaskTone.Pink),
    TaskItem("Сделаю домашку без напоминаний", R.drawable.tasks_ic_pencil, TaskTone.Blue),
    TaskItem("Помою пол", R.drawable.tasks_ic_drop, TaskTone.Orange),
    TaskItem("Накоплю деньги на своё желание", R.drawable.sk_ic_piggy, TaskTone.Pink),
    TaskItem("Приготовлю обед или ужин для всей семьи", R.drawable.tasks_ic_pot, TaskTone.Orange),
    TaskItem("Прочитаю книгу", R.drawable.tasks_ic_book, TaskTone.Blue),
    TaskItem("Постираю вещи в стиральной машине", R.drawable.tasks_ic_washer, TaskTone.Pink),
    TaskItem("Помою посуду за всей семьёй", R.drawable.tasks_ic_dishes, TaskTone.Orange),
    TaskItem("Поглажу свою одежду", R.drawable.tasks_ic_iron, TaskTone.Pink),
    TaskItem("Выучу таблицу умножения", R.drawable.tasks_ic_calc, TaskTone.Blue),
    TaskItem("Куплю продукты по списку", R.drawable.tasks_ic_cart, TaskTone.Orange),
    TaskItem("Поменяю постельное бельё", R.drawable.tasks_ic_bed, TaskTone.Pink),
    TaskItem("Изучу новую тему и расскажу её родителю", R.drawable.tasks_ic_bulb, TaskTone.Blue),
    TaskItem("Зашью дырку на одежде", R.drawable.tasks_ic_needle, TaskTone.Pink),
    TaskItem("Выучу 10 иностранных слов", R.drawable.tasks_ic_hat, TaskTone.Blue),
    TaskItem("Буду ложиться спать вовремя неделю", R.drawable.tasks_ic_moon, TaskTone.Pink),
)

internal val EasyTasks = listOf(
    TaskItem("Заправлю кровать", R.drawable.tasks_ic_bed, TaskTone.Pink),
    TaskItem("Полью цветы", R.drawable.tasks_ic_drop, TaskTone.Green),
    TaskItem("Покормлю питомца", R.drawable.tasks_ic_paw, TaskTone.Yellow),
    TaskItem("Соберу игрушки на место", R.drawable.tasks_ic_home, TaskTone.Orange),
    TaskItem("Соберу рюкзак в школу", R.drawable.tasks_ic_hat, TaskTone.Blue),
    TaskItem("Уберу посуду со стола", R.drawable.tasks_ic_dishes, TaskTone.Orange),
    TaskItem("Почитаю 15 минут", R.drawable.tasks_ic_book, TaskTone.Blue),
    TaskItem("Лягу спать без напоминаний", R.drawable.tasks_ic_moon, TaskTone.Pink),
)
