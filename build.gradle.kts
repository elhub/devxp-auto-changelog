import groovy.lang.GroovyObject
import org.jfrog.gradle.plugin.artifactory.dsl.PublisherConfig
import org.jfrog.gradle.plugin.artifactory.dsl.ResolverConfig

plugins {
    id("no.elhub.devxp.kotlin-core") version "0.1.2"
    `maven-publish`
    id("com.jfrog.artifactory") version "4.18.3"
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
        plugin("no.elhub.devxp.kotlin-core")
        plugin("maven-publish")
        plugin("com.jfrog.artifactory")
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
        setContextUrl("https://jfrog.elhub.cloud/artifactory")
        publish(delegateClosureOf<PublisherConfig> {
            repository(delegateClosureOf<GroovyObject> {
                setProperty(
                    "repoKey",
                    project.findProperty("targetRepoKey")
                        ?: throw NoSuchElementException("targetRepoKey property must be set")
                )
                setProperty("username", project.findProperty("artifactoryUsername") ?: "nouser")
                setProperty("password", project.findProperty("artifactoryPassword") ?: "nopass")
            })
            defaults(delegateClosureOf<GroovyObject> {
                invokeMethod("publications", subprojectName)
                setProperty("publishArtifacts", true)
                setProperty("publishPom", subprojectName == "core")
            })
        })
        resolve(delegateClosureOf<ResolverConfig> {
            setProperty("repoKey", "repo")
        })
    }
}

tasks.withType(Jar::class.java) {
    enabled = false // nothing to build in the root project
}
