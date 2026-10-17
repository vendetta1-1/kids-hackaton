package com.sber.rofles

import org.gradle.api.provider.Property

abstract class RoflesLibraryExtension {
    /** Android namespace, обязательный параметр (пример: "com.sber.rofles.core"). */
    abstract val namespace: Property<String>

    abstract val compileSdk: Property<Int>
    abstract val minSdk: Property<Int>
}