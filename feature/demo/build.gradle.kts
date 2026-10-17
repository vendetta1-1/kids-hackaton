plugins {
    id("com.sber.rofles.feature")
}

rofles {
    namespace = "com.sber.rofles.feature.demo"
}

dependencies {
    implementation(project(":core"))
    implementation(project(":domain"))
}