group = "no.elhub.devxp"

dependencies {
    val implementation by configurations
    val testImplementation by configurations

    implementation(libs.logging.slf4j.simple)
    testImplementation(libs.apache.commons.io)
    testImplementation(libs.test.kotlin.faker)
    testImplementation(libs.test.mockk)
}
