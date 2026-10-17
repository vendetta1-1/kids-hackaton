plugins {
    id("com.sber.rofles.feature")
}

rofles {
    namespace = "com.sber.rofles.feature.elementpicker"
}

dependencies {
    implementation(project(":core"))
    implementation(project(":domain"))
}