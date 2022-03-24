import com.adarshr.gradle.testlogger.theme.ThemeType
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import groovy.lang.GroovyObject
import org.jfrog.gradle.plugin.artifactory.dsl.PublisherConfig
import org.jfrog.gradle.plugin.artifactory.dsl.ResolverConfig

plugins {
    java
    kotlin("jvm") version "1.6.10" apply false
    id("com.adarshr.test-logger") version "2.1.1"
    id("com.github.ben-manes.versions") version "0.28.0" apply false
    id("io.qameta.allure") version "2.8.1" apply false
    id("com.github.johnrengelman.shadow") version "6.1.0" apply false
    `maven-publish`
    id("com.jfrog.artifactory") version "4.18.3"
    jacoco
}

group = "no.elhub.devxp"
description = "Automated changelog generation for git projects"

subprojects {
    group = parent?.group?.toString() ?: "no.elhub.core.eip"
    version = rootProject.version
    val artifactId = "${rootProject.name}-${this@subprojects.name}"

    repositories {
        maven("https://jfrog.elhub.cloud/artifactory/elhub-mvn/")
    }

    apply {
        plugin("java")
        plugin("org.jetbrains.kotlin.jvm")
        plugin("com.adarshr.test-logger")
        plugin("com.github.ben-manes.versions")
        plugin("io.qameta.allure")
        plugin("jacoco")
        plugin("maven-publish")
        plugin("com.jfrog.artifactory")
    }

    dependencies {
        val jgitVersion = "5.11.0.202103091610-r"
        implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.6.0"))
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.6.0")
        implementation("org.eclipse.jgit:org.eclipse.jgit:$jgitVersion")
        implementation("org.eclipse.jgit:org.eclipse.jgit.ssh.jsch:$jgitVersion")
    }

    tasks["assemble"].dependsOn(tasks["jar"])

    tasks.withType(Jar::class.java) {
        archiveBaseName.set(artifactId)
        manifest {
            attributes["Implementation-Title"] = artifactId
            attributes["Implementation-Version"] = rootProject.version
        }
    }

    publishing {
        publications {
            create<MavenPublication>(this@subprojects.name) {
                from(components["java"])
                pom {
                    setArtifactId(artifactId)
                }
            }
        }
    }

    tasks["publish"].dependsOn(tasks["artifactoryPublish"])
    tasks["artifactoryPublish"].dependsOn("assemble")

    artifactory {
        setContextUrl("https://jfrog.elhub.cloud/artifactory")
        publish(delegateClosureOf<PublisherConfig> {
            repository(delegateClosureOf<GroovyObject> {
                setProperty("repoKey", project.findProperty("targetRepoKey"))
                setProperty("username", project.findProperty("mavenuser") ?: "nouser")
                setProperty("password", project.findProperty("mavenpass") ?: "nopass")
            })
            defaults(delegateClosureOf<GroovyObject> {
                invokeMethod("publications", this@subprojects.name)
                setProperty("publishArtifacts", true)
            })
        })
        resolve(delegateClosureOf<ResolverConfig> {
            setProperty("repoKey", "repo")
        })
    }

    tasks.withType<DependencyUpdatesTask> {
        checkConstraints = true
        rejectVersionIf {
            isNonStable(candidate.version)
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            events("skipped", "failed")
            showStandardStreams = true
        }

        finalizedBy(tasks["jacocoTestReport"]) // report is always generated after tests run
    }

    testlogger {
        theme = ThemeType.MOCHA
        showPassed = false
    }

    jacoco {
        toolVersion = "0.8.7" // Has to be the same as TeamCity
    }

    tasks["jacocoTestReport"].apply {
        // only depends on 'test', but not 'kotest'
        dependsOn(tasks["test"]) // tests are required to run before generating the report
        enabled = false // TODO TD-1969
    }
}

tasks.withType<DependencyUpdatesTask> {
    checkConstraints = true
    rejectVersionIf {
        isNonStable(candidate.version)
    }
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
    val regex = "^[0-9,.v-]+(-r|-jre)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}
