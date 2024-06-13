import no.elhub.devxp.build.configuration.pipeline.ElhubProject.Companion.elhubProject
import no.elhub.devxp.build.configuration.pipeline.constants.Group.DEVXP
import no.elhub.devxp.build.configuration.pipeline.constants.ArtifactoryRepository
import no.elhub.devxp.build.configuration.pipeline.jobs.gradleAutoRelease
import no.elhub.devxp.build.configuration.pipeline.jobs.gradleVerify

elhubProject(DEVXP, "devxp-auto-changelog") {
    val artifactoryRepository = ArtifactoryRepository.ELHUB_BIN_RELEASE_LOCAL

    pipeline {
        sequential {
            val artifacts = gradleVerify {
                analyzeDependencies = false
                modules = listOf("cli", "core")
            }
            gradleAutoRelease(artifacts = listOf(artifacts)) {
                repository = artifactoryRepository
            }
        }
    }
}
