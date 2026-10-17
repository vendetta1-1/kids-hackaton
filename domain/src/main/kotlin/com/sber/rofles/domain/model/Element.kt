package com.sber.rofles.domain.model

/** Шесть стихий прикола «Дай пять!». */
enum class Element(val displayName: String) {
    FIRE("Огонь"),
    WATER("Вода"),
    ICE("Лёд"),
    AIR("Воздух"),
    EARTH("Земля"),
    LIGHTNING("Молния");

    companion object {
        const val COUNT = 6
    }
}