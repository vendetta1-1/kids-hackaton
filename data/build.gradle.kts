plugins {
    id("com.sber.rofles.feature")
}

android {
    namespace = "com.sber.rofles.data"
}

dependencies {
    implementation(project(":domain"))
    testImplementation(libs.bundles.test.unit)
}