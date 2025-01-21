plugins {
    id("no.elhub.devxp.kotlin-application") version "0.2.3"
    `maven-publish`
}

val applicationMainClass: String by project
application {
    mainClass = applicationMainClass
}

group = "no.elhub.devxp"
description = "Automated changelog generation for git projects"

subprojects {
    group = if (this.name == "cli") "" else parent?.group?.toString() ?: "no.elhub.devxp"
    version = rootProject.version
    val subproject = this@subprojects
    val subprojectName = subproject.name

    repositories {
        maven("https://jfrog.elhub.cloud/artifactory/elhub-mvn/")
    }

    apply {
        plugin("no.elhub.devxp.kotlin-application")
    }

    dependencies {
        implementation(platform(rootProject.libs.kotlin.bom))
        implementation(rootProject.libs.kotlin.stdlib.jdk8)
        implementation(rootProject.libs.git.jgit)
        implementation(rootProject.libs.git.jgit.ssh)
        testImplementation(rootProject.libs.test.kotest.runner.junit5)
    }

    tasks["assemble"].dependsOn(tasks["jar"])

    tasks.withType(Jar::class.java) {
        archiveBaseName.set(rootProject.name)
        manifest {
            attributes["Implementation-Title"] = rootProject.name
            attributes["Implementation-Version"] = rootProject.version
        }
    }

    publishing {
        publications {
            create<MavenPublication>(subprojectName) {
                groupId = subproject.group.toString()
                artifactId = rootProject.name
                version = subproject.version.toString()
                from(components["java"])
            }
        }
    }

    tasks["publish"].dependsOn(tasks["artifactoryPublish"])
    tasks["artifactoryPublish"].dependsOn("assemble")

    artifactory {
        publish {
            repository {
                setRepoKey(
                    when (project.name) {
                        "core" -> "elhub-mvn-release-local"
                        "cli" -> "elhub-bin-release-local"
                        else -> throw IllegalArgumentException("No repository configured for project ${project.name}")
                    }
                )
            }
        }
    }
}

tasks.withType(Jar::class.java) {
    enabled = false // nothing to build in the root project
}
