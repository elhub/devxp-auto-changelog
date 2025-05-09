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

    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("org.slf4j:slf4j-simple:2.0.17")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.8.1")
    implementation("commons-io:commons-io:2.18.0")
    implementation("io.github.serpro69:kotlin-faker:1.15.0")
    implementation("io.mockk:mockk:1.13.16")
    testImplementation("io.kotest:kotest-assertions-json:5.9.1")
}
