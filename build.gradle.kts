plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

val runDetekt = providers.gradleProperty("detekt").isPresent

if (runDetekt) {
    subprojects {
        buildscript {
            dependencies {
                classpath("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.23.9")
            }
        }
        apply(plugin = "io.gitlab.arturbosch.detekt")
    }

    tasks.register("detektAll") {
        group = "verification"
        dependsOn(subprojects.map { "${it.path}:detekt" })
    }
}