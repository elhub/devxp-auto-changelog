plugins {
}

dependencies {
    val implementation by configurations
    val testImplementation by configurations

    implementation(libs.bundles.logging.slf4j)
    testImplementation(libs.apache.commons.io)
    testImplementation(libs.test.kotlin.faker)
    testImplementation(libs.test.mockk)
}
