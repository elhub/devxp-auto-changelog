plugins {
    alias(libs.plugins.elhub.gradle.plugin.application)
    alias(libs.plugins.kotlin.plugin.serialization)
}

dependencies {
    implementation(libs.cli.picocli)
    implementation(libs.git.jgit)
    implementation(libs.bundles.ktor)

    testImplementation(libs.test.kotest.runner.junit5)
    testImplementation(libs.test.mockk)
}

application {
    mainClass.set("no.elhub.devxp.autochangelog.AutoChangelogKt")
}
