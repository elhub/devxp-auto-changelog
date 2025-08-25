import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("application")
}

application {
    mainClass = "no/elhub/devxp/autochangelog/cli/AutoChangelog.kt"
}

dependencies {
    implementation(project(":core"))
    implementation(libs.cli.picocli)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
        javaParameters = true
    }
}
