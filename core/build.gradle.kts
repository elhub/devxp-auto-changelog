group = "no.elhub.devxp"

plugins {
    kotlin("plugin.serialization") version "2.1.20"
}

kotlin {
    sourceSets {
        all {
            languageSettings.optIn("kotlinx.serialization.ExperimentalSerializationApi")
        }
    }
}

dependencies {
    val implementation by configurations
    val testImplementation by configurations

    implementation(libs.bundles.logging.slf4j)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.8.0")
    testImplementation(libs.apache.commons.io)
    testImplementation(libs.test.kotlin.faker)
    testImplementation(libs.test.mockk)
    testImplementation("io.kotest:kotest-assertions-json:5.9.1")
}
