plugins {
    alias(libs.plugins.elhub.gradle.plugin.application)
    alias(libs.plugins.kotlin.plugin.serialization)
}

dependencies {
    implementation(libs.cli.picocli)
    implementation(libs.git.jgit)
    implementation(libs.bundles.ktor)
    implementation(libs.logging.logback.classic)

    testImplementation(libs.test.kotest.runner.junit5)
    testImplementation(libs.test.mockk)
}

application {
    mainClass.set("no.elhub.devxp.autochangelog.AutoChangelogKt")
}

// This allows kotest to use reflection on JDK 9+. See https://github.com/kotest/kotest/issues/2849
tasks.withType<Test>().configureEach {
    jvmArgs(
        "--add-opens=java.base/java.util=ALL-UNNAMED"
    )
}
