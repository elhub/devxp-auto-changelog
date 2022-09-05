import jetbrains.buildServer.configs.kotlin.v2019_2.DslContext
import jetbrains.buildServer.configs.kotlin.v2019_2.project
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.VcsTrigger
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2019_2.version
import no.elhub.devxp.build.configuration.Assemble
import no.elhub.devxp.build.configuration.AutoRelease
import no.elhub.devxp.build.configuration.CodeReview
import no.elhub.devxp.build.configuration.ProjectType
import no.elhub.devxp.build.configuration.PublishDocs
import no.elhub.devxp.build.configuration.SonarScan
import no.elhub.devxp.build.configuration.UnitTest

version = "2022.04"

project {

    val projectId = "no.elhub.devxp:devxp-auto-changelog"
    val projectType = ProjectType.GRADLE
    val artifactoryRepository = "N/A" // this is set for each module individually

    params {
        param("teamcity.ui.settings.readOnly", "true")
    }

    val modules = listOf("cli", "core")

    val sonarScanConfig = SonarScan.Config(
        vcsRoot = DslContext.settingsRoot,
        type = projectType,
        sonarId = projectId,
        sonarProjectModules = modules,
    )

    val unitTest = UnitTest(
        UnitTest.Config(
            vcsRoot = DslContext.settingsRoot,
            type = projectType,
            generateAllureReport = false,
        )
    )

    val sonarScan = SonarScan(sonarScanConfig) {
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
    }

    val publishDocs = PublishDocs(
        PublishDocs.Config(
            vcsRoot = DslContext.settingsRoot,
            type = projectType,
            dest = "devxp/devxp-auto-changelog"
        )
    ) {
        dependencies {
            snapshot(autoRelease) { }
        }
        triggers {
            vcs {
                branchFilter = "+:<default>"
                quietPeriodMode = VcsTrigger.QuietPeriodMode.USE_DEFAULT
            }
        }
    }

    listOf(unitTest, sonarScan, assemble, autoRelease, publishDocs).forEach { buildType(it) }

    buildType(
        CodeReview(
            CodeReview.Config(
                vcsRoot = DslContext.settingsRoot,
                type = projectType,
                sonarScanConfig = sonarScanConfig,
            )
        )
    )
}
