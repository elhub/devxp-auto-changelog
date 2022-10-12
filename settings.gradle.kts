rootProject.name = "auto-changelog"

pluginManagement {
    repositories {
        maven(url = "https://jfrog.elhub.cloud:443/artifactory/elhub-plugins")
    }
}

include("cli")
include("core")
