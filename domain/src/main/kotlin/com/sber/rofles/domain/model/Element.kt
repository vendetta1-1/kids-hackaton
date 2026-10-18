package com.sber.rofles.domain.model

/** Семь стихий прикола «Дай пять!» — дают 21 комбинацию (7 по 2). */
enum class Element(val displayName: String) {
    FIRE("Огонь"),
    WATER("Вода"),
    ICE("Лёд"),
    AIR("Воздух"),
    EARTH("Земля"),
    LIGHTNING("Молния"),
    MAGIC("Магия");

    companion object {
        const val COUNT = 7
    }
}