import com.adarshr.gradle.testlogger.theme.ThemeType

plugins {
    alias(libs.plugins.elhub.gradle.plugin.library)
    alias(libs.plugins.kotlin.plugin.serialization)
}

dependencies {
    api(libs.git.jgit)
    api(libs.git.jgit.ssh)
    api(libs.kotlinx.serialization.json)
    implementation(libs.di.koin.core)
    implementation(libs.bundles.logging)
    implementation(libs.apache.commons.io)
    testImplementation(libs.test.kotest.runner.junit5)
    testImplementation(libs.test.kotest.extensions.koin)
    testImplementation(libs.test.kotlin.faker)
    testImplementation(libs.test.mockk)
    testImplementation(libs.test.kotest.assertions.json)
}

// Do not publish library artifacts to Artifactory
tasks.artifactoryPublish {
    skip = true
}

testlogger {
    showSimpleNames = true
    showStandardStreams = true
    showPassedStandardStreams = false
    showSkippedStandardStreams = false
    showFailedStandardStreams = true
}
