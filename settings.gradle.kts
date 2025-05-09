rootProject.name = "devxp-auto-changelog"

include("core")
include("cli")

pluginManagement {
    repositories {
        maven("https://jfrog.elhub.cloud:443/artifactory/elhub-mvn")
    }
}

dependencyResolutionManagement {
    repositories {
        maven("https://jfrog.elhub.cloud:443/artifactory/elhub-mvn")
    }
}
