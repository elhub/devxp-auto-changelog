plugins {
    id("no.elhub.devxp.kotlin-application") version "0.2.3"
}

group = ""

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(tasks.shadowJar)
        }
    }
}

dependencies {
    implementation(project(":core"))
    implementation(libs.cli.picocli)
    implementation(platform(rootProject.libs.kotlin.bom))
    implementation(rootProject.libs.git.jgit)
    implementation(rootProject.libs.git.jgit.ssh)
    testImplementation(rootProject.libs.test.kotest.runner.junit5)
}
