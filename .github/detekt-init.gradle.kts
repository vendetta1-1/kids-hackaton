// Init-скрипт для прогона detekt в CI. Подключается только через:
//   ./gradlew -I .github/detekt-init.gradle.kts detektAll
// В локальной сборке не участвует, поэтому не резолвит плагин и не ломает её
// (локально скачивание из Maven Central блокируется корпоративным SSL).
initscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    dependencies {
        classpath("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.23.9")
    }
}

gradle.projectsLoaded {
    gradle.rootProject.allprojects {
        apply(plugin = "io.gitlab.arturbosch.detekt")
    }
}

gradle.projectsEvaluated {
    val root = gradle.rootProject
    root.tasks.register("detektAll") {
        group = "verification"
        dependsOn(root.subprojects.map { "${it.path}:detekt" })
    }
}