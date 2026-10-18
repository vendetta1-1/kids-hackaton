package com.sber.rofles.core.prank

import androidx.annotation.DrawableRes
import com.sber.rofles.core.R
import com.sber.rofles.domain.model.ComboKey
import com.sber.rofles.domain.model.Element

object PrankArt {

    @DrawableRes
    fun element(element: Element): Int = when (element) {
        Element.FIRE -> R.drawable.prank_element_fire
        Element.WATER -> R.drawable.prank_element_water
        Element.EARTH -> R.drawable.prank_element_earth
        Element.AIR -> R.drawable.prank_element_air
        Element.LIGHTNING -> R.drawable.prank_element_lightning
        Element.ICE -> R.drawable.prank_element_ice
    }

    @DrawableRes
    fun combo(key: ComboKey): Int = when (key.id) {
        "1-1" -> R.drawable.prank_combo_1_1
        "1-2" -> R.drawable.prank_combo_1_2
        "1-3" -> R.drawable.prank_combo_1_3
        "1-4" -> R.drawable.prank_combo_1_4
        "1-5" -> R.drawable.prank_combo_1_5
        "1-6" -> R.drawable.prank_combo_1_6
        "2-2" -> R.drawable.prank_combo_2_2
        "2-3" -> R.drawable.prank_combo_2_3
        "2-4" -> R.drawable.prank_combo_2_4
        "2-5" -> R.drawable.prank_combo_2_5
        "2-6" -> R.drawable.prank_combo_2_6
        "3-3" -> R.drawable.prank_combo_3_3
        "3-4" -> R.drawable.prank_combo_3_4
        "3-5" -> R.drawable.prank_combo_3_5
        "3-6" -> R.drawable.prank_combo_3_6
        "4-4" -> R.drawable.prank_combo_4_4
        "4-5" -> R.drawable.prank_combo_4_5
        "4-6" -> R.drawable.prank_combo_4_6
        "5-5" -> R.drawable.prank_combo_5_5
        "5-6" -> R.drawable.prank_combo_5_6
        "6-6" -> R.drawable.prank_combo_6_6
        else -> R.drawable.prank_extra_secret
    }

    @DrawableRes
    val highFive: Int = R.drawable.prank_extra_high_five

    @DrawableRes
    val bluetooth: Int = R.drawable.prank_extra_bluetooth

    @DrawableRes
    val album: Int = R.drawable.prank_extra_album

    @DrawableRes
    val back: Int = R.drawable.prank_ic_back

    @DrawableRes
    val check: Int = R.drawable.prank_ic_check

    @DrawableRes
    val clock: Int = R.drawable.prank_ic_clock
}
