package com.sber.rofles

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

/**
 * Convention-плагин для Android feature-модулей.
 *
 * Применяет базовый набор плагинов (Android Library, Kotlin, KSP, Hilt,
 * Compose-компилятор) и подключает все стандартные зависимости модуля:
 * Dagger Hilt, Jetpack Compose, Navigation 3 (Nav3) и kotlinx-coroutines.
 *
 * Использование в build.gradle.kts модуля:
 * ```
 * plugins { id("com.sber.rofles.feature") }
 * rofles { namespace = "com.sber.rofles.core" }
 * ```
 */
class RoflesFeaturePlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.library")
            pluginManager.apply("com.google.devtools.ksp")
            pluginManager.apply("com.google.dagger.hilt.android")
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            val rofles = extensions.create("rofles", RoflesFeatureExtension::class.java)
            rofles.compileSdk.convention(37)
            rofles.minSdk.convention(26)

            extensions.configure<LibraryExtension>("android") {
                compileSdk = rofles.compileSdk.get()

                defaultConfig {
                    minSdk = rofles.minSdk.get()
                }

                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_21
                    targetCompatibility = JavaVersion.VERSION_21
                }

                buildFeatures {
                    compose = true
                }
            }

            extensions.configure<KotlinAndroidProjectExtension>("kotlin") {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_21)
                }
            }

            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            dependencies {
                "implementation"(platform(libs.findLibrary("androidx-compose-bom").get().get()))
                "implementation"(libs.findLibrary("androidx-core-ktx").get().get())
                "implementation"(libs.findLibrary("androidx-activity-compose").get().get())
                "implementation"(libs.findBundle("lifecycle").get())
                "implementation"(libs.findBundle("compose").get())
                "implementation"(libs.findLibrary("androidx-navigation3-ui").get().get())
                "implementation"(libs.findBundle("hilt").get())
                "implementation"(libs.findBundle("coroutines").get())
                "implementation"(libs.findLibrary("lottie-compose").get().get())
                "ksp"(libs.findLibrary("hilt-compiler").get().get())
                "debugImplementation"(libs.findLibrary("androidx-compose-ui-tooling").get().get())
            }
        }
    }
}