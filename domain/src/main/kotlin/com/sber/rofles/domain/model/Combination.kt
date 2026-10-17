package com.sber.rofles.domain.model

/**
 * Комбинация двух стихий. Порядок не важен: огонь+вода == вода+огонь,
 * поэтому пара хранится отсортированной по порядку [Element.ordinal].
 */
data class Combination(val first: Element, val second: Element) {

    init {
        require(first != second) { "Комбинация из двух одинаковых стихий невозможна" }
    }

    /** Уникальный идентификатор комбинации (0..20). */
    val id: Int = combinationIndex(first, second)

    companion object {
        /** Все 21 комбинации из 6 стихий по 2. */
        val ALL: List<Combination> =
            Element.entries.flatMapIndexed { i, a ->
                Element.entries.drop(i + 1).map { b -> Combination(a, b) }
            }

        private fun combinationIndex(a: Element, b: Element): Int {
            val (lo, hi) = if (a.ordinal < b.ordinal) a to b else b to a
            var idx = 0
            for (i in 0 until lo.ordinal) idx += (Element.COUNT - i - 1)
            return idx + (hi.ordinal - lo.ordinal - 1)
        }
    }
}