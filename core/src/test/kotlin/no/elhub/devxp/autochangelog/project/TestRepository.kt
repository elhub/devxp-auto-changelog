package no.elhub.devxp.autochangelog.project

import no.elhub.devxp.autochangelog.extensions.delete
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ResetCommand
import java.nio.file.Path
import java.nio.file.Paths

object TestRepository {
    private val tempFolderPath = Paths.get("build/keep-a-changelog/").also {
        if (it.toFile().exists()) it.delete()
    }

    val git: Git by lazy {
        Git.cloneRepository()
            .setDirectory(tempFolderPath.toFile())
            .setURI("https://github.com/elhub/devxp-auto-changelog.git")
            .call().apply {
                reset()
                    .setMode(ResetCommand.ResetType.HARD)
                    .setRef("4b898e6")
                    .call()
            }
    }

    val changelogPath: Path = tempFolderPath.resolve("CHANGELOG.md")

    init {
        // clone the repo
        git
    }
}
