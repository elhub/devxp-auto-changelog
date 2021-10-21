import java.net.URI

description = "Automated changelog generation for git projects"

val kotestVersion = "4.4.1"
val jgitVersion = "5.11.0.202103091610-r"
val mockkVersion = "1.10.6"

repositories {
    maven {
        url = URI("https://oss.sonatype.org/content/repositories/snapshots/")
    }
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.eclipse.jgit:org.eclipse.jgit:$jgitVersion")
    implementation("org.eclipse.jgit:org.eclipse.jgit.ssh.jsch:$jgitVersion")
    implementation("info.picocli:picocli:4.6.1")
    implementation("org.slf4j:slf4j-api:1.7.30")
    implementation("org.slf4j:slf4j-simple:1.7.30")
    testImplementation("commons-io:commons-io:2.8.0")
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-extensions-allure-jvm:$kotestVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("io.github.serpro69:kotlin-faker:1.9.0-SNAPSHOT")
}
