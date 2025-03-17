plugins {
    id("no.elhub.devxp.kotlin-core") version "0.2.3"
    id("maven-publish")
    id("com.jfrog.artifactory") version "5.2.5"
}

group = "no.elhub.devxp"

subprojects {
    val subproject = this@subprojects

    apply {
        plugin("no.elhub.devxp.kotlin-core")
        plugin("maven-publish")
        plugin("com.jfrog.artifactory")
    }

    dependencies {
        implementation(platform(rootProject.libs.kotlin.bom))
        implementation(rootProject.libs.git.jgit)
        implementation(rootProject.libs.git.jgit.ssh)
        testImplementation(rootProject.libs.test.kotest.runner.junit5)
    }

    publishing {
        publications {
            create<MavenPublication>(rootProject.name) {
                groupId = rootProject.group.toString()
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
