plugins {
    id("com.sber.rofles.feature")
}

android {
    namespace = "com.sber.rofles.data"
}

dependencies {
    implementation(project(":domain"))
    implementation(libs.androidx.datastore.preferences)
    testImplementation(libs.bundles.test.unit)
}