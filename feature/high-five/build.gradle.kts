plugins {
    id("com.sber.rofles.feature")
}

android {
    namespace = "com.sber.rofles.feature.highfive"
}

dependencies {
    implementation(project(":core"))
    implementation(project(":domain"))
}