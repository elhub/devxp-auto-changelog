package no.elhub.devxp.autochangelog

import org.eclipse.jgit.api.InitCommand
import java.nio.file.Path
import java.time.Instant
import java.time.ZoneId
import kotlin.io.path.createTempDirectory
import kotlin.io.path.writeText
import org.eclipse.jgit.lib.PersonIdent

fun createMockResponse(key: String, title: String, body: String): String = """
        {
            "expand": "renderedFields,names,schema,operations,editmeta,changelog,versionedRepresentations",
            "id": "1",
            "self": "https://google.atlassian.net/rest/api/3/issue/1",
            "key": "$key",
            "fields": {
                "summary": "$title",
                "description": {
                    "type": "doc",
                    "version": 1,
                    "content": [
                        {
                            "type": "paragraph",
                            "content": [
                                {
                                    "type": "text",
                                    "text": "$body"
                                }
                            ]
                        }
                    ]
                }
            }
        }
""".trimIndent()

data class TestCommit(
    val fileName: String,
    val content: String,
    val message: String,
    val tags: List<String> = emptyList()
)

fun createRepositoryFromCommits(name: String, commits: List<TestCommit>): Path {
    val tempDir = createTempDirectory(name)
    val baseTime = Instant.now()

    InitCommand().setDirectory(tempDir.toFile()).call().use { git ->
        commits.forEachIndexed { index, commit ->
            val f = tempDir.resolve(name)
            f.writeText(commit.content)
            git.add().addFilepattern(name).call()

            // Calculate timestamp: use explicit offset if provided, otherwise use index
            val timestamp = baseTime.plusSeconds(index.toLong())

            val c = git.commit()
                .setMessage(commit.message)
                .setCommitter(PersonIdent("Test User", "test@example.com", timestamp, ZoneId.systemDefault()))
                .call()

            if (commit.tags.isNotEmpty()) {
                commit.tags.forEach { tag ->
                    git
                        .tag()
                        .setName(tag)
                        .setAnnotated(false)
                        .setObjectId(c)
                        .call()
                }
            }
        }
    }
    return tempDir
}

fun createNonGitDirectory(): Path {
    val tempDir = createTempDirectory("not-a-git-repo")
    tempDir.toFile().deleteOnExit()
    return tempDir
}
