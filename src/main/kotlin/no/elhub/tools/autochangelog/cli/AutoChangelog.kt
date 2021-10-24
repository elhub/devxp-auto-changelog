package no.elhub.tools.autochangelog.cli

import no.elhub.tools.autochangelog.io.ChangelogWriter
import no.elhub.tools.autochangelog.project.GitRepo
import org.eclipse.jgit.api.Git
import picocli.CommandLine
import java.io.File
import java.util.concurrent.Callable
import kotlin.system.exitProcess

@CommandLine.Command(
    name = "auto-changelog",
    mixinStandardHelpOptions = true,
    description = ["auto-changelog ."],
    optionListHeading = "@|bold %nOptions|@:%n",
    versionProvider = ManifestVersionProvider::class,
    sortOptions = false
)
object AutoChangelog : Callable<Int> {

    override fun call(): Int {
        val git = Git.open(File("."))
        val repo = GitRepo(git)
        val changelist = repo.createChangelist(repo.constructLog())
        File("CHANGELOG.md").writer().use {
            it.write(ChangelogWriter().writeToString(changelist))
            it.flush()
        }
        return 0
    }

}

object ManifestVersionProvider : CommandLine.IVersionProvider {

    @Throws(Exception::class)
    override fun getVersion(): Array<String> {
        return arrayOf(CommandLine::class.java.`package`.implementationVersion.toString())
    }

}

@Suppress("SpreadOperator")
fun main(args: Array<String>): Unit = exitProcess(
    CommandLine(AutoChangelog).execute(*args)
)
