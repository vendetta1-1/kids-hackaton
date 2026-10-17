plugins {
    id("com.sber.rofles.feature")
}

rofles {
    namespace = "com.sber.rofles.data"
}

dependencies {
    implementation(project(":domain"))
}