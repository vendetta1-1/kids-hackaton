package com.sber.rofles.core.effect

/**
 * Решает, на каких экранах тряска запускает эффект.
 * Денежные экраны (перевод, QR, деталка карты) — запрещены.
 */
object ScreenGuard {

    private val allowed = setOf(
        PrikolScreen.MAIN,
        PrikolScreen.HISTORY,
        PrikolScreen.THANKS,
        PrikolScreen.PIGGYBANK,
        PrikolScreen.TASKS,
        PrikolScreen.CONTENT,
        PrikolScreen.PROFILE,
    )

    fun allows(screen: PrikolScreen): Boolean = screen in allowed
}