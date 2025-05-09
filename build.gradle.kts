plugins {
    id("no.elhub.devxp.kotlin-core") version "0.5.0"
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
        implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

        implementation("org.eclipse.jgit:org.eclipse.jgit:6.7.0.202309050840-r")
        implementation("org.eclipse.jgit:org.eclipse.jgit.ssh.jsch:6.7.0.202309050840-r")
        implementation("io.kotest:kotest-runner-junit5:5.8.0")
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
