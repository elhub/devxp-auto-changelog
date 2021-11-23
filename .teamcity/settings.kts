import jetbrains.buildServer.configs.kotlin.v2019_2.DslContext
import jetbrains.buildServer.configs.kotlin.v2019_2.project
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.VcsTrigger
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2019_2.version
import no.elhub.common.build.configuration.Assemble
import no.elhub.common.build.configuration.AutoRelease
import no.elhub.common.build.configuration.CodeReview
import no.elhub.common.build.configuration.ProjectType
import no.elhub.common.build.configuration.SonarScan
import no.elhub.common.build.configuration.UnitTest

version = "2021.2"

project {

    val projectId = "no.elhub.devxp:devxp-auto-changelog"
    val projectType = ProjectType.GRADLE
    val artifactoryRepository = "elhub-bin-release-local"

    params {
        param("teamcity.ui.settings.readOnly", "true")
    }

    val unitTest = UnitTest(
        UnitTest.Config(
            vcsRoot = DslContext.settingsRoot,
            type = projectType
        )
    )

    val sonarScan = SonarScan(
        SonarScan.Config(
            vcsRoot = DslContext.settingsRoot,
            type = projectType,
            sonarId = projectId
        )
    ) {
        dependencies {
            snapshot(unitTest) { }
        }
    }

    val assemble = Assemble(
        Assemble.Config(
            vcsRoot = DslContext.settingsRoot,
            type = projectType
        )
    ) {
        dependencies {
            snapshot(sonarScan) { }
        }
    }

    val autoRelease = AutoRelease(
        AutoRelease.Config(
            vcsRoot = DslContext.settingsRoot,
            type = projectType,
            repository = artifactoryRepository
        )
    ) {
        dependencies {
            snapshot(assemble) { }
        }

        triggers {
            vcs {
                branchFilter = "+:<default>"
                quietPeriodMode = VcsTrigger.QuietPeriodMode.USE_DEFAULT
            }
        }
    }

    val publishDocs =         buildType(
        PublishDocs(
            PublishDocs.Config(
                vcsRoot = DslContext.settingsRoot,
                type = projectType,
                dest = "devxp/devxp-auto-changelog"
            )
        ) {
            triggers {
                vcs {
                    branchFilter = "+:<default>"
                    quietPeriodMode = VcsTrigger.QuietPeriodMode.USE_DEFAULT
                }
            }
        }
    )

    listOf(unitTest, sonarScan, assemble, autoRelease, publishDocs).forEach { buildType(it) }

    buildType(
        CodeReview(
            CodeReview.Config(
                vcsRoot = DslContext.settingsRoot,
                type = projectType,
                sonarId = projectId
            )
        )
    )
}
