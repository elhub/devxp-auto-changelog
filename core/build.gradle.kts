plugins {
    alias(libs.plugins.elhub.gradle.plugin.library)
    alias(libs.plugins.kotlin.plugin.serialization)
    // alias(libs.plugins.kotlin.plugin.serialization)
}

dependencies {
    api(rootProject.libs.git.jgit)
    api(rootProject.libs.git.jgit.ssh)
    api(libs.kotlinx.serialization.json)
    implementation(libs.bundles.logging)
    implementation(libs.apache.commons.io)
    testImplementation(libs.test.kotest.runner.junit5)
    testImplementation(libs.test.kotlin.faker)
    testImplementation(libs.test.mockk)
    testImplementation(libs.test.kotest.assertions.json)
}

// Do not publish library artifacts to Artifactory
tasks.artifactoryPublish {
    skip = true
}
