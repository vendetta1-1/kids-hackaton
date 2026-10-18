package com.sber.rofles.domain.usecase

import com.sber.rofles.domain.model.ComboKey
import com.sber.rofles.domain.model.Element

object AvatarFrames {
    val combosPerElement: Int = Element.entries.size

    fun progress(opened: Collection<ComboKey>, element: Element): Int =
        opened.toSet().count { it.a == element || it.b == element }

    fun completed(opened: Collection<ComboKey>): Set<Element> =
        Element.entries.filter { progress(opened, it) >= combosPerElement }.toSet()

    fun pick(completed: Set<Element>, mine: Element?): Element? = mine?.takeIf { it in completed }
}
