import jetbrains.buildServer.configs.kotlin.ArtifactRule
import no.elhub.devxp.build.configuration.pipeline.constants.Group.DEVXP
import no.elhub.devxp.build.configuration.pipeline.dsl.elhubProject
import no.elhub.devxp.build.configuration.pipeline.jobs.gradleAutoRelease
import no.elhub.devxp.build.configuration.pipeline.jobs.gradleVerify

elhubProject(DEVXP, "devxp-auto-changelog") {
    val gradleModules = listOf("core", "cli")

    pipeline {
        sequential {
            val artifacts = gradleVerify {
                analyzeDependencies = false
                modules = gradleModules
                buildArtifactRules = gradleModules.map { ArtifactRule.include("$it/build", "$it/build.zip") }
                outputArtifactRules = gradleModules.map { ArtifactRule.include("$it/build.zip!**", "$it/build") }
                enablePublishMetrics = true
            }
            gradleAutoRelease(artifacts = listOf(artifacts)) {
                gradleModule = "cli"
            }
        }
    }
}
