package patches.buildTypes

import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.SSHUpload
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.sshUpload
import jetbrains.buildServer.configs.kotlin.v2019_2.ui.*

/*
This patch script was generated by TeamCity on settings change in UI.
To apply the patch, change the buildType with id = 'PublishDocs'
accordingly, and delete the patch script.
*/
changeBuildType(RelativeId("PublishDocs")) {
    expectSteps {
        script {
            name = "Generate Changelog"
            scriptContent = "%auto-changelog% -o 'docs/pages/about/' -f 'changelog.md'"
        }
        script {
            name = "Build Docs"
            scriptContent = """
                mkdir build
                PATH=${'$'}PATH:%KOTLIN_PATH%; kscript https://raw.githubusercontent.com/elhub/devxp-linux-scripts/v0.1.1/scripts/orchid-run.kts build
            """.trimIndent()
        }
        sshUpload {
            name = "Publish Docs"
            transportProtocol = SSHUpload.TransportProtocol.SCP
            sourcePath = "build/docs/orchid/ => ."
            targetUrl = "docs.elhub.cloud:/u01/www/docs/devxp/devxp-auto-changelog"
            authMethod = uploadedKey {
                username = "docspublisher"
                passphrase = "%docspublisher.passphrase%"
                key = "teamcity_docs_rsa"
            }
        }
    }
    steps {
        update<SSHUpload>(2) {
            clearConditions()
            authMethod = uploadedKey {
                username = "docspublisher"
                passphrase = "credentialsJSON:1d9c28c3-957a-404f-945d-0a4a648c1ad5"
                key = "teamcity_docs_rsa"
            }
        }
    }
}
