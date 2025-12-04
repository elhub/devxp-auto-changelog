package no.elhub.devxp.autochangelog

import org.eclipse.jgit.api.InitCommand
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlin.io.path.writeText

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
    InitCommand().setDirectory(tempDir.toFile()).call().use { git ->
        commits.forEach { commit ->
            val f = tempDir.resolve(name)
            f.writeText(commit.content)
            git.add().addFilepattern(name).call()
            val c = git.commit().setMessage(commit.message).call()
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
