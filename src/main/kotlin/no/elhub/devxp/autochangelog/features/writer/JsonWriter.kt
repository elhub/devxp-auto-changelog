package no.elhub.devxp.autochangelog.features.writer

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import no.elhub.devxp.autochangelog.features.git.GitCommit
import no.elhub.devxp.autochangelog.features.jira.JiraIssue
import java.io.File
import java.time.LocalDate.now

fun formatJson(jiraIssues: Map<JiraIssue, List<GitCommit>>): String {
    val json = buildJsonObject {
        put("generatedAt", now().toString())

        val noJiraEntry = jiraIssues.entries.filter { it.key.key == "NO-JIRA" }

        putJsonArray("issues") {
            jiraIssues
                .filterNot { it.key.key == "NO-JIRA" }
                .forEach { (jiraIssue, commits) ->
                    addJsonObject {
                        put("key", jiraIssue.key)
                        put("title", jiraIssue.title)
                        put("body", jiraIssue.body)
                        putJsonArray("commits") {
                            commits.forEach { commit ->
                                add(commit.toJsonObject())
                            }
                        }
                    }
                }
        }

        if (noJiraEntry.isNotEmpty()) {
            putJsonArray("commitsWithoutJira") {
                noJiraEntry.first().value.forEach { commit ->
                    add(commit.toJsonObject())
                }
            }
        }
    }
    val prettyJson = Json { prettyPrint = true }
    return prettyJson.encodeToString(json)
}

fun formatCommitJson(commitsMap: Map<GitCommit, List<JiraIssue>>): String {
    val json = buildJsonObject {
        put("generatedAt", now().toString())

        putJsonArray("commits") {
            commitsMap.forEach { (commit, jiraIssues) ->
                addJsonObject {
                    val commitJson = commit.toJsonObject()
                    commitJson.forEach { key, value ->
                        put(key, value)
                    }

                    putJsonArray("tags") {
                        commit.tags.forEach { tag -> add(tag.name) }
                    }

                    putJsonArray("issues") {
                        if (jiraIssues.firstOrNull()?.key != "NO-JIRA") {
                            jiraIssues.forEach { jiraIssue ->
                                addJsonObject {
                                    put("key", jiraIssue.key)
                                    put("title", jiraIssue.title)
                                    put("body", jiraIssue.body)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    val prettyJson = Json { prettyPrint = true }
    return prettyJson.encodeToString(json)
}

private fun GitCommit.toJsonObject(): JsonObject = buildJsonObject {
    put("hash", hash)
    put("title", title)
    put("body", body)
    put("commitTime", commitTime.toString())
    putJsonArray("tags") {
        tags.forEach { tag ->
            addJsonObject {
                put("name", tag.name)
                put("commitHash", tag.commitHash)
            }
        }
    }
}

fun writeJsonToFile(json: String, filePath: String) {
    val file = File(filePath)
    file.parentFile?.mkdirs()
    file.writeText(json)
}
