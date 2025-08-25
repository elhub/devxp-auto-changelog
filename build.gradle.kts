plugins {
    id("no.elhub.devxp.kotlin-core") version "0.7.10"
    id("maven-publish")
    id("com.jfrog.artifactory") version "6.0.0"
}

subprojects {
    val subproject = this@subprojects
    val subprojectName = subproject.name

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
                setPublishIvy(false) // Publish generated Ivy descriptor files to Artifactory (true by default)
            }
        }
    }

    tasks["publish"].dependsOn(tasks["artifactoryPublish"])
}
