package no.elhub.devxp.autochangelog.cli

import java.io.File
import java.util.concurrent.Callable
import kotlin.io.path.ExperimentalPathApi
import kotlin.system.exitProcess
import no.elhub.devxp.autochangelog.config.Configuration
import no.elhub.devxp.autochangelog.extensions.description
import no.elhub.devxp.autochangelog.git.GitLog
import no.elhub.devxp.autochangelog.io.ChangelogReader
import no.elhub.devxp.autochangelog.io.ChangelogWriter
import no.elhub.devxp.autochangelog.project.GitRepo
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.ObjectId
import picocli.CommandLine

@ExperimentalPathApi
@CommandLine.Command(
    name = "auto-changelog",
    mixinStandardHelpOptions = true,
    description = ["auto-changelog ."],
    optionListHeading = "@|bold %nOptions|@:%n",
    versionProvider = ManifestVersionProvider::class,
    sortOptions = false
)
object AutoChangelog : Callable<Int> {

    @CommandLine.Option(
        names = ["-d", "--dir-path"],
        required = false,
        description = ["Path to directory with git repository.", "Defaults to '.'"]
    )
    private var repoPath: String = "."

    @CommandLine.Option(
        names = ["-n", "--changelog-name"],
        required = false,
        description = ["Input changelog file name.", "Defaults to 'CHANGELOG.md'"]
    )
    private var inputFileName: String = "CHANGELOG.md"

    @CommandLine.Option(
        names = ["-o", "--output-dir"],
        required = false,
        description = ["Output directory path to which changelog file will be written.", "Defaults to '.'"]
    )
    private var outputDir: String = "."

    @CommandLine.Option(
        names = ["-f", "--file-name"],
        required = false,
        description = ["Output file name.", "Defaults to 'CHANGELOG.md'"]
    )
    private var outputFileName: String = "CHANGELOG.md"

    @CommandLine.Option(
        names = ["--no-date"],
        required = false,
        description = ["Generate changelog without dates."]
    )
    private var noDate: Boolean = false

    override fun call(): Int {
        val repoDir = File(repoPath)
        val git = Git.open(repoDir)
        val repo = GitRepo(git)
        val changelogFile = repoDir.resolve(inputFileName)
        val content = if (changelogFile.exists() && changelogFile.isFile) {
            val lastRelease = ChangelogReader(changelogFile.toPath()).getLastRelease()
            val end = lastRelease?.let { repo.findCommitId(it) }
            val changelist = repo.createChangelist(repo.getLog(end = end))
            ChangelogWriter(changelogFile.toPath(), noDate).writeToString(changelist)
        } else {
            val changelist = repo.createChangelist(repo.getLog())
            ChangelogWriter(noDate).writeToString(changelist)
        }

        File(outputDir)
            .apply { createDirIfNotExists() ?: return 1 }
            .resolve(outputFileName)
            .apply { createFileIfNotExists() ?: return 1 }
            .writer().use {
                it.write(content)
                it.flush()
            }
        return 0
    }

    private fun GitRepo.getLog(end: ObjectId? = null): GitLog = constructLog(end = end) {
        if (Configuration.includeOnlyWithJira) {
            it.description.any { s -> s.startsWith(Configuration.jiraIssuesPatternString) } || tags().any { t ->
                (git.repository.refDatabase.peel(t).peeledObjectId ?: t.objectId) == it.toObjectId()
            }
        } else true
    }

    private fun File.createDirIfNotExists(): Boolean? = when {
        !exists() -> mkdirs()
        !isDirectory -> {
            println("'${this.path}' is not a directory")
            null
        }
        else -> false
    }

    private fun File.createFileIfNotExists(): Boolean? = when {
        !exists() -> createNewFile()
        !isFile -> {
            println("'${this.path}' is not a regular file")
            null
        }
        else -> false
    }
}

object ManifestVersionProvider : CommandLine.IVersionProvider {

    @Throws(Exception::class)
    override fun getVersion(): Array<String> {
        return arrayOf(CommandLine::class.java.`package`.implementationVersion.toString())
    }

}

@OptIn(ExperimentalPathApi::class)
@Suppress("SpreadOperator")
fun main(args: Array<String>): Unit = exitProcess(
    CommandLine(AutoChangelog).execute(*args)
)
