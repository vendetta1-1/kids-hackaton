package com.sber.rofles.ui

import com.sber.rofles.ui.shell.components.SkTab

enum class Route {
    MAIN,
    PIGGY_BANKS,
    TASKS,
    CONTENT,

    TRANSFER,
    SOUNDS,

    ALBUM,
    SETTINGS,
    DEMO,
}

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
