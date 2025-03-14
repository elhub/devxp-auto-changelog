group = "no.elhub.devxp"

dependencies {
    val implementation by configurations
    val testImplementation by configurations

    implementation(libs.bundles.logging.slf4j)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.8.0")

    testImplementation(libs.apache.commons.io)
    testImplementation(libs.test.kotlin.faker)
    testImplementation(libs.test.mockk)
}
