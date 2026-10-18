plugins {
    id("com.sber.rofles.feature")
}

android {
    namespace = "com.sber.rofles.prank"
}

dependencies {
    api(project(":core"))
    api(project(":domain"))
    implementation(project(":data"))
    implementation(project(":feature:solo-effect"))
    implementation(project(":feature:high-five"))
    implementation(project(":feature:element-picker"))
    implementation(project(":feature:album"))
    implementation(project(":feature:demo"))
}
