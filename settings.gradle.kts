@file:Suppress("UnstableApiUsage")

rootProject.name = "devxp-auto-changelog"

include("cli")
include("core")

pluginManagement {
    repositories {
        maven("https://jfrog.elhub.cloud:443/artifactory/elhub-mvn")
        maven("https://jfrog.elhub.cloud:443/artifactory/elhub-plugins")
    }
}

dependencyResolutionManagement {
    repositories {
        maven("https://jfrog.elhub.cloud:443/artifactory/elhub-mvn")
    }

    versionCatalogs {
        create("libs") {
            from("no.elhub.devxp:devxp-version-catalog:0.5.1")
        }
    }
}
