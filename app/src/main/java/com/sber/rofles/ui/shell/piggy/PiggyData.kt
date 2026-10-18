package com.sber.rofles.ui.shell.piggy

import androidx.annotation.DrawableRes
import com.sber.rofles.R

internal sealed interface PiggyImage {
    data class Icon(@DrawableRes val icon: Int, val tint: Long?, val background: Long) : PiggyImage

    data class Photo(@DrawableRes val image: Int) : PiggyImage
}

internal data class PiggyBank(
    val title: String,
    val rubles: String,
    val kopecks: String? = null,
    val goal: String? = null,
    val progress: Float = 0f,
    val image: PiggyImage,
)

internal data class OthersGoal(
    val title: String,
    val price: String,
    @DrawableRes val image: Int,
)

private val PlainPiggy = PiggyImage.Icon(R.drawable.sk_ic_piggy, tint = 0xFFB3BBDC, background = 0xFFEBEFFF)

internal const val PiggyTotalRubles = "16 158"
internal const val PiggyYearRate = "11%"
internal const val PiggyArchiveCount = "6 копилок"

internal val PiggyBanksTop = listOf(
    PiggyBank(
        title = "Копилка фаворит",
        rubles = "30",
        image = PiggyImage.Icon(R.drawable.piggy_ic_piggy_error, tint = null, background = 0xFFFFEAE0),
    ),
    PiggyBank(title = "Копилка", rubles = "0", image = PlainPiggy),
    PiggyBank(
        title = "Yeezy Slide",
        rubles = "1 675",
        kopecks = "50",
        goal = "из 3 000 ₽",
        progress = 0.4f,
        image = PiggyImage.Photo(R.drawable.piggy_img_yeezy),
    ),
)

internal const val PiggyDoneWithdraw = "Снять 7 989,32 ₽"

internal val PiggyBanksBottom = listOf(
    PiggyBank(
        title = "Неоновая вывеска и зеркало Hello Kitty",
        rubles = "3000",
        goal = "из 7600 ₽",
        progress = 0.4f,
        image = PiggyImage.Photo(R.drawable.piggy_img_hello_kitty),
    ),
    PiggyBank(title = "Слайм розовый", rubles = "580", goal = "из 700 ₽", progress = 0.72f, image = PlainPiggy),
    PiggyBank(
        title = "Фломастеры от А4",
        rubles = "100",
        kopecks = "32",
        goal = "из 200 ₽",
        progress = 0.7f,
        image = PlainPiggy,
    ),
    PiggyBank(
        title = "Фитнес-браслет Xiaomi Mi Band 7",
        rubles = "1 800",
        goal = "из 2 000 ₽",
        progress = 0.6f,
        image = PiggyImage.Photo(R.drawable.piggy_img_band),
    ),
    PiggyBank(
        title = "Робот Макс",
        rubles = "3 950",
        goal = "из 12 000 ₽",
        progress = 0.4f,
        image = PiggyImage.Photo(R.drawable.piggy_img_robot),
    ),
)

internal val OthersGoals = listOf(
    OthersGoal("Фитнес-браслет Xiaomi Mi Band 7", "2 000 000 000", R.drawable.piggy_img_markers),
    OthersGoal("Набор скетч-маркеров, 80 цветов", "2 490", R.drawable.piggy_img_markers),
    OthersGoal("Фитнес-браслет Xiaomi Mi Band 7", "3 990", R.drawable.piggy_img_band),
    OthersGoal("Робот Макс", "12 000", R.drawable.piggy_img_robot),
)
