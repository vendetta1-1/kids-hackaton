plugins {
    id("com.sber.rofles.feature")
}

rofles {
    namespace = "com.sber.rofles.core"
}

dependencies {
    implementation(project(":domain"))
}