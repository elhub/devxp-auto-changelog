import org.owasp.dependencycheck.reporting.ReportGenerator

plugins {
    id("no.elhub.devxp.kotlin-core") version "0.1.0"
}

description = "Automated changelog generation for git projects"

subprojects {
    apply(plugin = "no.elhub.devxp.kotlin-core")
    group = if (this.name == "cli") "" else parent?.group?.toString() ?: "no.elhub.devxp"
    version = rootProject.version

    dependencies {
        implementation(platform(rootProject.libs.kotlin.bom))
        implementation(rootProject.libs.kotlin.stdlib.jdk8)
        implementation(rootProject.libs.git.jgit)
        implementation(rootProject.libs.git.jgit.ssh)
        testImplementation(rootProject.libs.test.kotest.runner.junit5)
    }

}

tasks.withType(Jar::class.java) {
    enabled = false // nothing to build in the root project
}

dependencyCheck {
    formats = listOf(
        ReportGenerator.Format.JSON,
        ReportGenerator.Format.HTML,
    )
}
