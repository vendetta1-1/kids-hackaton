package com.sber.rofles

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

/**
 * Convention-плагин для Android-library модулей.
 *
 * Применяет базовый набор плагинов (Android Library, Kotlin, KSP, Hilt,
 * Compose-компилятор) и подключает все стандартные зависимости модуля:
 * Dagger Hilt, Jetpack Compose, Navigation 3 (Nav3) и kotlinx-coroutines.
 *
 * Использование в build.gradle.kts модуля:
 * ```
 * plugins { id("com.sber.rofles.library") }
 * rofles { namespace = "com.sber.rofles.core" }
 * ```
 */
class RoflesLibraryPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.library")
            pluginManager.apply("org.jetbrains.kotlin.android")
            pluginManager.apply("com.google.devtools.ksp")
            pluginManager.apply("com.google.dagger.hilt.android")
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            val rofles = extensions.create("rofles", RoflesLibraryExtension::class.java)
            rofles.namespace.convention("")
            rofles.compileSdk.convention(37)
            rofles.minSdk.convention(26)

            extensions.configure<LibraryExtension>("android") {
                namespace = rofles.namespace.get()
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

            dependencies {
                "implementation"(platform(libs.androidx.compose.bom))
                "implementation"(libs.androidx.core.ktx)
                "implementation"(libs.androidx.activity.compose)
                "implementation"(libs.bundles.lifecycle)
                "implementation"(libs.bundles.compose)
                "implementation"(libs.androidx.navigation3.ui)
                "implementation"(libs.bundles.hilt)
                "implementation"(libs.bundles.coroutines)
                "implementation"(libs.lottie.compose)
                "ksp"(libs.hilt.compiler)
                "debugImplementation"(libs.androidx.compose.ui.tooling)
            }
        }
    }
}