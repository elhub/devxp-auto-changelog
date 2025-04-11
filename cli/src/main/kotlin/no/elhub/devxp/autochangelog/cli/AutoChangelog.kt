package no.elhub.devxp.autochangelog.cli

import java.io.File
import java.util.concurrent.Callable
import kotlin.io.path.ExperimentalPathApi
import kotlin.system.exitProcess
import no.elhub.devxp.autochangelog.config.Configuration.INCLUDE_ONLY_WITH_JIRA
import no.elhub.devxp.autochangelog.config.Configuration.JIRA_ISSUES_PATTERN_STRING
import no.elhub.devxp.autochangelog.extensions.description
import no.elhub.devxp.autochangelog.extensions.title
import no.elhub.devxp.autochangelog.git.GitLog
import no.elhub.devxp.autochangelog.git.bareClone
import no.elhub.devxp.autochangelog.io.ChangelogReader
import no.elhub.devxp.autochangelog.io.ChangelogWriter
import no.elhub.devxp.autochangelog.project.Changelist
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
        names = ["-r", "--remote-path"],
        required = false,
        description = ["Url to remote repository", "Defaults to '.'"]
    )
    private var remotePath: String = ""

    @CommandLine.Option(
        names = ["-j", "--json"],
        required = false,
        description = ["Whether to write the changelog as json", "Defaults to 'false'"]
    )
    private var asJson: Boolean = false

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

    override fun call(): Int {
        if (remotePath.isNotBlank()) {
            bareClone(remotePath, repoPath)
        }
        if (asJson) {
            outputFileName = outputFileName.replace(".md", ".json")
        }

        val repoDir = File(repoPath)
        val git = Git.open(repoDir)
        val repo = GitRepo(git)
        val changelogFile = repoDir.resolve(inputFileName)

        lateinit var changeList: Changelist
        lateinit var content: String

        when {
            // When JSON output is specified, we generate a changelog based on the entire git log
            asJson -> {
                changeList = repo.createChangelist(repo.getLog())
                content = ChangelogWriter().writeToJson(changeList)
            }
            // When a CHANGELOG.md file already exists, we only need to fetch logs for commits not in that file
            changelogFile.exists() && changelogFile.isFile -> {
                val lastRelease = ChangelogReader(changelogFile.toPath()).getLastRelease()
                val end = lastRelease?.let { repo.findCommitId(it) }
                changeList = repo.createChangelist(repo.getLog(end = end))
                content = ChangelogWriter(changelogFile.toPath()).writeToString(changeList)
            }
            // When there is no CHANGELOG.md file, we generate a changelog based on the entire git log
            else -> {
                changeList = repo.createChangelist(repo.getLog())
                content = ChangelogWriter().writeToString(changeList)
            }
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
        if (INCLUDE_ONLY_WITH_JIRA) {
            it.description.any { s -> s.startsWith(JIRA_ISSUES_PATTERN_STRING) }
                    || it.title.matches(Regex("""^.*[A-Z][A-Z0-9]+-\d+.*${'$'}"""))
                    || it.description.any { line -> line.matches(Regex("""^.*[A-Z][A-Z0-9]+-\d+.*${'$'}""")) }
                    || tags().any { t ->
                (git.repository.refDatabase.peel(t).peeledObjectId ?: t.objectId) == it.toObjectId()
            }
        } else {
            true
        }
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
    override fun getVersion(): Array<String> = arrayOf(CommandLine::class.java.`package`.implementationVersion.toString())
}

@OptIn(ExperimentalPathApi::class)
@Suppress("SpreadOperator")
fun main(args: Array<String>): Unit = exitProcess(
    CommandLine(AutoChangelog).execute(*args)
)
