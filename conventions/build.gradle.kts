plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

group = "com.sber.rofles"
version = "1.0.0"

dependencies {
    implementation(libs.android.gradle.plugin)
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.kotlin.compose.gradle.plugin)
    implementation(libs.ksp.gradle.plugin)
    implementation(libs.hilt.gradle.plugin)
}

gradlePlugin {
    plugins {
        create("roflesFeature") {
            id = "com.sber.rofles.feature"
            implementationClass = "com.sber.rofles.RoflesFeaturePlugin"
        }
    }
}