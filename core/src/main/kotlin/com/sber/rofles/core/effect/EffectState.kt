package com.sber.rofles.core.effect

import com.sber.rofles.domain.model.Combination
import com.sber.rofles.domain.model.Element

enum class EffectKind {
    SOLO,
    HIGH_FIVE,
}

/** Что сейчас показываем в глобальном оверлее. */
data class EffectState(
    val kind: EffectKind,
    val element: Element? = null,
    val combination: Combination? = null,
    val animationUrl: String? = null,
)