import no.elhub.devxp.build.configuration.pipeline.ElhubProject.Companion.elhubProject
import no.elhub.devxp.build.configuration.pipeline.constants.Group.DEVXP
import no.elhub.devxp.build.configuration.pipeline.jobs.gradleAutoRelease
import no.elhub.devxp.build.configuration.pipeline.jobs.gradleVerify

elhubProject(DEVXP, "devxp-auto-changelog") {

    pipeline {
        sequential {
            val artifacts = gradleVerify {
                analyzeDependencies = false
            }
            gradleAutoRelease(artifacts = listOf(artifacts))
        }
    }
}
