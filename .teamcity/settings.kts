import no.elhub.devxp.build.configuration.pipeline.constants.ArtifactoryRepository.ELHUB_BIN_RELEASE_LOCAL
import no.elhub.devxp.build.configuration.pipeline.constants.Group.DEVXP
import no.elhub.devxp.build.configuration.pipeline.dsl.elhubProject
import no.elhub.devxp.build.configuration.pipeline.jobs.gradlePublish
import no.elhub.devxp.build.configuration.pipeline.jobs.gradleVerify

elhubProject(DEVXP, "devxp-auto-changelog") {

    pipeline {
        sequential {
            val artifacts = gradleVerify {
                enablePublishMetrics = true
            }

            gradlePublish(artifacts = listOf(artifacts)) {
                repository = ELHUB_BIN_RELEASE_LOCAL
            }
        }
    }
}
