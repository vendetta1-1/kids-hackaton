package com.sber.rofles.domain.setup

object PrankOptions {
    const val DEFAULT_CONFIG_URL =
        "https://raw.githubusercontent.com/vendetta1-1/kids-hackaton/main/config/kids_prank_high_five.json"

    @Volatile
    var configUrl: String = DEFAULT_CONFIG_URL

    @Volatile
    var avatarIndex: () -> Int = { 0 }
}
