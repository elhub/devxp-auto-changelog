plugins {
    id("no.elhub.devxp.kotlin-library") version "0.2.3"
    `maven-publish`
}

group = "no.elhub.devxp"
description = "Automated changelog generation for git projects"

subprojects {
    version = rootProject.version

    apply {
        if (project.name == "cli") {
            plugin("no.elhub.devxp.kotlin-application")
        } else {
            plugin("no.elhub.devxp.kotlin-library")
        }
    }

    dependencies {
        implementation(platform(rootProject.libs.kotlin.bom))
        implementation(rootProject.libs.git.jgit)
        implementation(rootProject.libs.git.jgit.ssh)
        testImplementation(rootProject.libs.test.kotest.runner.junit5)
    }
}
