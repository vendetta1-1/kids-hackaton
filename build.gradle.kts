plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

// detekt подключается только при -Pdetekt (передаётся в CI).
// Это нужно, чтобы локальная сборка не падала: плагин не лежит в локальном кэше,
// а скачивание из Maven Central на рабочей машине блокируется корпоративным SSL.
val runDetekt = providers.gradleProperty("detekt").isPresent

if (runDetekt) {
    subprojects {
        buildscript {
            dependencies {
                classpath("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:${libs.versions.detekt.get()}")
            }
        }
        apply(plugin = "io.gitlab.arturbosch.detekt")
    }

    tasks.register("detektAll") {
        group = "verification"
        dependsOn(subprojects.map { "${it.path}:detekt" })
    }
}