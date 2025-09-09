group = "no.elhub.devxp"

plugins {
    alias(libs.plugins.serialization)
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

    implementation(libs.bundles.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.apache.commons.io)
    implementation(libs.test.kotlin.faker)
    implementation(libs.test.mockk)
    testImplementation(libs.test.kotest.assertions.json)
}

tasks.artifactoryPublish {
    skip = true
}
