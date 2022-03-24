package no.elhub.devxp.autochangelog.project

import no.elhub.devxp.autochangelog.extensions.delete
import org.eclipse.jgit.api.Git
import java.nio.file.Path
import java.nio.file.Paths

object TestRepository {
    private val tempFolderPath = Paths.get("build/keep-a-changelog/").also {
        if (it.toFile().exists()) it.delete()
    }

    val git: Git by lazy {
        Git.cloneRepository()
            .setDirectory(tempFolderPath.toFile())
            .setURI("https://github.com/elhub/keep-a-changelog.git")
            .call()
    }

    val changelogPath: Path = tempFolderPath.resolve("CHANGELOG.md")

    init {
        // clone the repo
        git
    }
}
