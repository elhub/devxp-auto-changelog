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
        val gitDir = tempFolderPath.toFile()
        gitDir.mkdirs()

        ProcessBuilder("git", "clone", "https://github.com/elhub/devxp-auto-changelog.git", gitDir.absolutePath)
            .inheritIO()
            .start()
            .waitFor()

        ProcessBuilder("git", "reset", "--hard", "4b898e6")
            .directory(gitDir)
            .inheritIO()
            .start()
            .waitFor()

        Git.open(gitDir)
    }

    val changelogPath: Path = tempFolderPath.resolve("CHANGELOG.md")

    init {
        // clone the repo
        git
    }
}
