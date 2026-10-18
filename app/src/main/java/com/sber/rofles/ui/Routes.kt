package com.sber.rofles.ui

import com.sber.rofles.ui.shell.components.SkTab

/** Типизированные маршруты приложения (Nav3: ключи стека). */
enum class Route {
    // Оболочка СберKids: вкладки нижнего меню.
    MAIN,
    PIGGY_BANKS,
    TASKS,
    CONTENT,

    // Оболочка: вложенные экраны.
    TRANSFER,
    SOUNDS,

    // Прикол.
    SOLO,
    HIGH_FIVE,
    ELEMENT_PICKER,
    ALBUM,
    SETTINGS,
    DEMO,
}

/** Вкладка таббара, которой соответствует маршрут; null — таббар не показываем. */
val Route.tab: SkTab?
    get() = when (this) {
        Route.MAIN -> SkTab.HOME
        Route.PIGGY_BANKS -> SkTab.PIGGY_BANKS
        Route.TASKS -> SkTab.TASKS
        Route.CONTENT -> SkTab.CONTENT
        else -> null
    }

val SkTab.route: Route
    get() = when (this) {
        SkTab.HOME -> Route.MAIN
        SkTab.PIGGY_BANKS -> Route.PIGGY_BANKS
        SkTab.TASKS -> Route.TASKS
        SkTab.CONTENT -> Route.CONTENT
    }
