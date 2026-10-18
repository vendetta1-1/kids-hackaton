package com.sber.rofles.domain.model

@ConsistentCopyVisibility
data class ComboKey private constructor(val a: Element, val b: Element) {

    val id: String get() = "${a.id}-${b.id}"

    val isTwins: Boolean get() = a == b

    companion object {
        fun of(x: Element, y: Element): ComboKey = if (x.id <= y.id) ComboKey(x, y) else ComboKey(y, x)

        fun parse(id: String): ComboKey? {
            val parts = id.split("-")
            if (parts.size != 2) return null
            val a = parts[0].toIntOrNull()?.let(Element::fromId) ?: return null
            val b = parts[1].toIntOrNull()?.let(Element::fromId) ?: return null
            return of(a, b)
        }

        val ALL: List<ComboKey> = Element.entries.flatMapIndexed { index, a ->
            Element.entries.drop(index).map { b -> of(a, b) }
        }
    }
}
