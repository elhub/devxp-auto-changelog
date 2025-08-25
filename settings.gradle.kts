rootProject.name = "devxp-auto-changelog"

include("core")
include("cli")

pluginManagement {
    repositories {
        maven {
            url = uri("https://jfrog.elhub.cloud:443/artifactory/elhub-mvn")
            mavenContent {
                releasesOnly()
            }
        }
    }
}
