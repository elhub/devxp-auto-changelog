plugins {
    id("no.elhub.devxp.kotlin-library") version "0.2.3"
}

group = "no.elhub.devxp"

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}

dependencies {
    val implementation by configurations
    val testImplementation by configurations

    implementation(libs.bundles.logging.slf4j)
    testImplementation(libs.apache.commons.io)
    testImplementation(libs.test.kotlin.faker)
    testImplementation(libs.test.mockk)
    implementation(platform(rootProject.libs.kotlin.bom))
    implementation(rootProject.libs.git.jgit)
    implementation(rootProject.libs.git.jgit.ssh)
    testImplementation(rootProject.libs.test.kotest.runner.junit5)
}
