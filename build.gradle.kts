plugins {
    alias(libs.plugins.elhub.gradle.plugin.application)
    alias(libs.plugins.kotlin.plugin.serialization)
}

dependencies {
    implementation(libs.cli.picocli)
    implementation("org.eclipse.jgit:org.eclipse.jgit:6.8.0.202311291450-r")
    testImplementation(libs.test.kotest.assertions.json)
    testImplementation(libs.test.kotest.runner.junit5)
    testImplementation(libs.test.mockk)

}

application {
    mainClass.set("AutoChangelogKt")
}
