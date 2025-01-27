import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.1.0"
    id("maven-publish")
    id("com.jfrog.artifactory") version "5.2.5"
}

repositories {
    maven(url = "https://jfrog.elhub.cloud/artifactory/elhub-mvn/")
}

subprojects {
    val subproject = this@subprojects
    val subprojectName = subproject.name

    apply {
        plugin("kotlin")
        plugin("maven-publish")
        plugin("com.jfrog.artifactory")
    }

    repositories {
        maven(url = "https://jfrog.elhub.cloud/artifactory/elhub-mvn/")
    }

    dependencies {
        implementation(platform(rootProject.libs.kotlin.bom))
        implementation(rootProject.libs.git.jgit)
        implementation(rootProject.libs.git.jgit.ssh)
        testImplementation(rootProject.libs.test.kotest.runner.junit5)
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
            javaParameters = true
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

    artifactory {
        clientConfig.isIncludeEnvVars = true

        publish {
            contextUrl = project.findProperty("artifactoryUri")?.toString() ?: "https://jfrog.elhub.cloud/artifactory"
            repository {
                repoKey = project.findProperty("artifactoryRepository")?.toString() ?: "elhub-mvn-dev-local"
                username = project.findProperty("artifactoryUsername")?.toString() ?: "nouser" // The publisher user name
                password = project.findProperty("artifactoryPassword")?.toString() ?: "nopass" // The publisher password
            }

            defaults {
                publications("ALL_PUBLICATIONS")
                setPublishArtifacts(true)
                setPublishPom(true) // Publish generated POM files to Artifactory (true by default)
                setPublishIvy(false) // Publish generated Ivy descriptor files to Artifactory (true by default)
            }
        }
    }

    tasks["publish"].dependsOn(tasks["artifactoryPublish"])
}
