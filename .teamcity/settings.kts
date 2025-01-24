import jetbrains.buildServer.configs.kotlin.ArtifactRule
import no.elhub.devxp.build.configuration.pipeline.ElhubProject.Companion.elhubProject
import no.elhub.devxp.build.configuration.pipeline.constants.Group.DEVXP
import no.elhub.devxp.build.configuration.pipeline.constants.ArtifactoryRepository
import no.elhub.devxp.build.configuration.pipeline.jobs.gradleAutoRelease
import no.elhub.devxp.build.configuration.pipeline.jobs.gradleVerify

elhubProject(DEVXP, "devxp-auto-changelog") {
    val gradleModules = listOf("core", "cli")
    val artifactoryRepository = ArtifactoryRepository.ELHUB_BIN_RELEASE_LOCAL

    pipeline {
        sequential {
            val artifacts = gradleVerify {
                analyzeDependencies = false
                modules = gradleModules
                buildArtifactRules = gradleModules.map { ArtifactRule.include("$it/build", "$it/build.zip") }
                outputArtifactRules = gradleModules.map { ArtifactRule.include("$it/build.zip!**", "$it/build") }
            }
            gradleAutoRelease(artifacts = listOf(artifacts)) {
                repository = artifactoryRepository
            }
        }
    }
}
