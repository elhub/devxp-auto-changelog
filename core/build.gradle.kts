dependencies {
    val kotestVersion = "4.4.3"
    val mockkVersion = "1.12.4"
    val implementation by configurations
    val testImplementation by configurations

    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("org.slf4j:slf4j-simple:1.7.36")
    testImplementation("commons-io:commons-io:2.11.0")
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-extensions-allure-jvm:$kotestVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("io.github.serpro69:kotlin-faker:1.11.0")
}
