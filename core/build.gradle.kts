plugins {
    id("com.sber.rofles.feature")
}

android {
    namespace = "com.sber.rofles.core"
}

dependencies {
    implementation(project(":domain"))
    testImplementation(libs.bundles.test.unit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
}