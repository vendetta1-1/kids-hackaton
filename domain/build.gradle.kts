plugins {
    `java-library`
    kotlin("jvm")
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.bundles.test.unit)
}