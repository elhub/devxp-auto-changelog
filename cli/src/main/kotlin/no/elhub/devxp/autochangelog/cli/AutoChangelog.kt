package no.elhub.devxp.autochangelog.cli

import no.elhub.devxp.autochangelog.config.Configuration.INCLUDE_ONLY_WITH_JIRA
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
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevWalk
import picocli.CommandLine
import java.io.File
import java.nio.file.Files
import java.util.concurrent.Callable
import kotlin.io.path.ExperimentalPathApi
import kotlin.system.exitProcess

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

    @CommandLine.Option(
        names = ["--up-to"],
        required = false,
        description = ["Include commits up to and including the specified tag."]
    )
    private var upToTag: String? = null

    @CommandLine.Option(
        names = ["--after"],
        required = false,
        description = ["Include commits after the specified tag (excluding the tag itself)."]
    )
    private var afterTag: String? = null

    @CommandLine.Option(
        names = ["--jira"],
        required = false,
        description = ["Filter commits to include only those with Jira issues and fetch Jira details."]
    )
    private var jiraEnabled: Boolean = false

    override fun call(): Int {
        var tempDir: File? = null
        try {
            // Initialize Jira integration if enabled
            if (jiraEnabled) {
                println("Initializing Jira integration...")
                val initialized = no.elhub.devxp.autochangelog.jira.JiraIssueExtractor.initialize()
                if (!initialized) {
                    System.err.println("Failed to initialize Jira integration. JIRA_USERNAME and JIRA_TOKEN environment variables must be set.")
                    return 1
                }
            }

            // If using remote repository, create a temporary directory for the clone
            val workingRepoPath = if (remotePath.isNotBlank()) {
                println("Cloning remote repository: $remotePath this can take up to ~30 seconds")
                tempDir = Files.createTempDirectory("auto-changelog-").toFile()
                println("Using temporary directory for clone: ${tempDir.absolutePath}")
                bareClone(remotePath, tempDir.absolutePath)
                println("Repository cloned successfully")
                tempDir.absolutePath
            } else {
                repoPath
            }

            if (asJson) {
                outputFileName = outputFileName.replace(".md", ".json")
            }

            val repoDir = File(workingRepoPath)

            // Check if the directory is a git repository
            // For bare repositories, the directory itself is the git repo (no .git folder)
            val isGitRepo = if (remotePath.isNotBlank()) {
                // For remote repos we don't need to check, we just cloned it
                true
            } else {
                val gitDir = File(repoDir, ".git")
                gitDir.exists() || File(repoDir, "HEAD").exists() // Check for bare repo
            }

            if (!isGitRepo && remotePath.isBlank()) {
                System.err.println("Error: Directory '$repoPath' is not a git repository.")
                System.err.println("Please use the -r/--remote-path option to specify a remote repository if you're not in a git repository.")
                return 1
            }

            println("Opening git repository in: ${repoDir.absolutePath}")
            val git = Git.open(repoDir)
            val repo = GitRepo(git)

            // Validate and resolve tags if specified
            val resolvedUpToCommit = upToTag?.let { resolveTag(git.repository, it) }
            val resolvedAfterCommit = afterTag?.let { resolveTag(git.repository, it) }

            // Check if afterTag is newer than upToTag when both are specified
            if (resolvedUpToCommit != null && resolvedAfterCommit != null) {
                val revWalk = RevWalk(git.repository)
                try {
                    val upToCommit = revWalk.parseCommit(resolvedUpToCommit)
                    val afterCommit = revWalk.parseCommit(resolvedAfterCommit)

                    // Check if afterCommit is an ancestor of upToCommit
                    val isOlderOrEqual = revWalk.isMergedInto(afterCommit, upToCommit)
                    if (!isOlderOrEqual) {
                        System.err.println("Error: The --after tag '$afterTag' is newer than the --up-to tag '$upToTag'.")
                        System.err.println("The --after tag must be older than the --up-to tag.")
                        return 1
                    }
                } finally {
                    revWalk.close()
                }
            }

            val changelogFile = repoDir.resolve(inputFileName)

            lateinit var changeList: Changelist
            lateinit var content: String

            when {
                // When JSON output is specified, we generate a changelog based on the entire git log
                asJson -> {
                    changeList = repo.createChangelist(repo.getLog(upToCommit = resolvedUpToCommit, afterCommit = resolvedAfterCommit))
                    content = ChangelogWriter(includeJiraDetails = jiraEnabled).writeToJson(changeList)
                }
                // When a CHANGELOG.md file already exists, we only need to fetch logs for commits not in that file
                changelogFile.exists() && changelogFile.isFile && upToTag == null && afterTag == null -> {
                    val lastRelease = ChangelogReader(changelogFile.toPath()).getLastRelease()
                    val end = lastRelease?.let { repo.findCommitId(it) }
                    changeList = repo.createChangelist(repo.getLog(end = end))
                    content = ChangelogWriter(changelogFile.toPath(), includeJiraDetails = jiraEnabled).writeToString(changeList)
                }
                // When there is no CHANGELOG.md file or tag filters are specified, we generate a changelog based on filtered git log
                else -> {
                    changeList = repo.createChangelist(repo.getLog(upToCommit = resolvedUpToCommit, afterCommit = resolvedAfterCommit))

                    // If changelog file exists and we're using tag filters, still use it for styling
                    val writer = if (changelogFile.exists() && changelogFile.isFile) {
                        ChangelogWriter(changelogFile.toPath(), includeJiraDetails = jiraEnabled)
                    } else {
                        ChangelogWriter(includeJiraDetails = jiraEnabled)
                    }

                    content = writer.writeToString(changeList)
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
        } catch (e: Exception) {
            System.err.println("Error: Failed to process repository. ${e.message}")
            return 1
        } finally {
            tempDir?.deleteRecursively()
        }
    }

    private fun GitRepo.getLog(
        end: ObjectId? = null,
        upToCommit: ObjectId? = null,
        afterCommit: ObjectId? = null
    ): GitLog {
        println("Generating changelog with the following filters:")
        if (upToCommit != null) {
            println("  - Including commits up to: ${upToCommit.name}")
        }
        if (afterCommit != null) {
            println("  - Including commits after: ${afterCommit.name}")
        }
        if (jiraEnabled) {
            println("  - Filtering commits to only include those with Jira issues")
        }

        // Get all commits in the repository
        val allCommits = git.log().call().toList()

        // Filter commits based on the specified range
        val filteredCommits = if (upToCommit != null && afterCommit != null) {
            // Find the indices of the boundary commits
            val upToIndex = allCommits.indexOfFirst { it.id == upToCommit }
            val afterIndex = allCommits.indexOfFirst { it.id == afterCommit }

            if (upToIndex == -1) {
                println("Warning: Could not find the 'up-to' commit in the repository.")
                emptyList()
            } else if (afterIndex == -1) {
                println("Warning: Could not find the 'after' commit in the repository.")
                emptyList()
            } else {
                // Take commits between afterIndex (exclusive) and upToIndex (inclusive)
                // Note: Git log returns commits in reverse chronological order (newest first)
                if (afterIndex < upToIndex) {
                    // This means afterCommit is newer than upToCommit, which is reversed from what we want
                    println("Warning: After tag is newer than up-to tag, swapping the order.")
                    allCommits.subList(afterIndex + 1, upToIndex + 1)
                } else {
                    allCommits.subList(upToIndex, afterIndex)
                }
            }
        } else if (upToCommit != null) {
            // Find the index of the upToCommit
            val upToIndex = allCommits.indexOfFirst { it.id == upToCommit }

            if (upToIndex == -1) {
                println("Warning: Could not find the 'up-to' commit in the repository.")
                emptyList()
            } else {
                // Take all commits up to and including upToCommit
                allCommits.subList(0, upToIndex + 1)
            }
        } else if (afterCommit != null) {
            // Find the index of the afterCommit
            val afterIndex = allCommits.indexOfFirst { it.id == afterCommit }

            if (afterIndex == -1) {
                println("Warning: Could not find the 'after' commit in the repository.")
                emptyList()
            } else {
                // Take all commits after afterCommit (excluding afterCommit itself)
                allCommits.subList(0, afterIndex)
            }
        } else if (end != null) {
            // Original case - use the end parameter
            constructLog(end = end) {
                isCommitIncluded(it)
            }.commits.map { it.objectId }.map { objId ->
                allCommits.find { it.id == objId }
            }.filterNotNull()
        } else {
            // No filters, include all commits
            allCommits
        }

        println("Found ${filteredCommits.size} commits matching the criteria.")

        // Apply additional filtering (JIRA tickets, etc.)
        val finalFilteredCommits = filteredCommits.filter { commit ->
            // If jiraEnabled is true, only include commits with Jira issues
            val includeBasedOnJira = if (jiraEnabled) {
                val jiraIssues = no.elhub.devxp.autochangelog.jira.JiraIssueExtractor.extractJiraIssuesFromCommit(commit)
                val hasJiraIssues = jiraIssues.isNotEmpty()

                if (hasJiraIssues) {
                    // Fetch Jira issues data if there are issues in the commit
                    no.elhub.devxp.autochangelog.jira.JiraIssueExtractor.fetchJiraIssues(jiraIssues)
                }

                hasJiraIssues
            } else {
                true
            }

            includeBasedOnJira && isCommitIncluded(commit)
        }

        if (jiraEnabled) {
            println("After applying Jira filter: ${finalFilteredCommits.size} commits with Jira issues.")
        } else {
            println("After applying JIRA filter: ${finalFilteredCommits.size} commits.")
        }

        // Manually construct a GitLog using the filtered commits
        return constructLog(predicate = { commit ->
            finalFilteredCommits.any { it.id == commit.id }
        })
    }

    private fun GitRepo.isCommitIncluded(commit: RevCommit): Boolean {
        if (INCLUDE_ONLY_WITH_JIRA) {
            return commit.title.matches(Regex("""^.*[A-Z][A-Z0-9]+-\d+.*${'$'}""")) ||
                commit.description.any { line -> line.matches(Regex("""^.*[A-Z][A-Z0-9]+-\d+.*${'$'}""")) } ||
                tags().any { t ->
                    (git.repository.refDatabase.peel(t).peeledObjectId ?: t.objectId) == commit.id
                }
        }
        return true
    }

    /**
     * Resolves a tag name to a commit ObjectId
     * @param repository The git repository
     * @param tagName The tag name to resolve
     * @return The ObjectId of the commit the tag points to
     * @throws IllegalArgumentException if the tag cannot be resolved
     */
    private fun resolveTag(repository: Repository, tagName: String): ObjectId {
        // Try potential tag name variations
        val tagNames = listOf(
            tagName, // Original form
            if (tagName.startsWith("v")) tagName.substring(1) else "v$tagName" // Try with/without 'v'
        ).distinct()

        var lastException: Exception? = null

        for (name in tagNames) {
            try {
                val tagObjectId = repository.resolve(name)
                    ?: continue // Try next variation

                // Try to parse as a commit directly
                val revWalk = RevWalk(repository)
                try {
                    return revWalk.parseCommit(tagObjectId).id
                } catch (e: Exception) {
                    // If we get here, the tag doesn't point directly to a commit
                    // It might be pointing to an annotated tag object
                    val refName = "refs/tags/$name"
                    val tagRef = repository.findRef(refName)

                    if (tagRef != null) {
                        // Try to peel the tag to find the commit it points to
                        val peeledTag = repository.refDatabase.peel(tagRef)
                        val peeledObjectId = peeledTag.peeledObjectId ?: tagRef.objectId

                        try {
                            // Try to parse as a commit
                            return revWalk.parseCommit(peeledObjectId).id
                        } catch (e2: Exception) {
                            lastException = IllegalArgumentException("Tag '$name' does not point to a commit. Please use a tag that points to a commit.")
                            // Try next variation
                        }
                    } else {
                        lastException = IllegalArgumentException("Failed to find tag reference for '$name'")
                        // Try next variation
                    }
                } finally {
                    revWalk.close()
                }
            } catch (e: Exception) {
                if (e is IllegalArgumentException) {
                    lastException = e
                } else {
                    lastException = IllegalArgumentException("Error resolving tag '$name': ${e.message}")
                }
                // Try next variation
            }
        }

        // If we get here, none of the tag variations worked
        throw lastException ?: IllegalArgumentException("Tag '$tagName' not found. Available tags: ${listAvailableTags(repository)}")
    }

    /**
     * Lists all available tags in the repository
     * @param repository The git repository
     * @return A comma-separated list of tag names
     */
    private fun listAvailableTags(repository: Repository): String {
        val tags = repository.refDatabase.getRefsByPrefix("refs/tags/")
            .map { it.name.removePrefix("refs/tags/") }

        return if (tags.isEmpty()) {
            "No tags found in repository"
        } else {
            tags.joinToString(", ")
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
