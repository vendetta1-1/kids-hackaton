package com.sber.rofles.domain.model

enum class Element(val id: Int) {
    FIRE(1),
    WATER(2),
    EARTH(3),
    AIR(4),
    LIGHTNING(5),
    ICE(6);

    companion object {
        fun fromId(id: Int): Element? = entries.firstOrNull { it.id == id }
    }
}
