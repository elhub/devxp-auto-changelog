plugins {
    alias(libs.plugins.elhub.gradle.plugin.application)
    alias(libs.plugins.kotlin.plugin.serialization)
}

dependencies {
    implementation(project(":core"))
    implementation(libs.cli.picocli)
    implementation(libs.di.koin.core)
    testImplementation(libs.test.kotest.runner.junit5)
}
