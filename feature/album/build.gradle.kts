plugins {
    id("com.sber.rofles.feature")
}

rofles {
    namespace = "com.sber.rofles.feature.album"
}

dependencies {
    implementation(project(":core"))
    implementation(project(":domain"))
}