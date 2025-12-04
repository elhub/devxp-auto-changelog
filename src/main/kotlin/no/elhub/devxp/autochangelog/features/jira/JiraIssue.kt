package no.elhub.devxp.autochangelog.features.jira

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

data class JiraIssue(
    val key: String,
    val title: String,
    val body: String,
)

@Serializable
data class JiraApiResponse(
    val key: String,
    val fields: JiraFields
)

@Serializable
data class JiraFields(
    val summary: String,
    val description: AdfDocument? = null
)

fun JiraApiResponse.toJiraIssue(): JiraIssue = JiraIssue(
    key = key,
    title = fields.summary,
    body = fields.description.toPlainText()
)

// Atlassian returns their description in Atlassian Document Format (ADF)
// See https://developer.atlassian.com/cloud/jira/platform/apis/document/structure/
@Serializable
data class AdfDocument(
    val type: String,
    val version: Int,
    val content: List<AdfNode>? = null
)

@Serializable
data class AdfNode(
    val type: String,
    val content: List<AdfContent>? = null,
    val text: String? = null,
    val attrs: Map<String, JsonElement>? = null
)

@Serializable
data class AdfContent(
    val type: String,
    val text: String? = null,
    val content: List<AdfContent>? = null,
    val marks: List<AdfMark>? = null
)

@Serializable
data class AdfMark(
    val type: String
)

/**
 * Converts an [AdfDocument] to plain text by extracting all text nodes.
 *
 * @receiver An optional [AdfDocument] to convert.
 * @return A [String] containing the concatenated plain text from the document.
 */
fun AdfDocument?.toPlainText(): String {
    if (this == null) return ""

    val texts = mutableListOf<String>()

    fun extractText(content: List<AdfContent>?) {
        content?.forEach { node ->
            node.text?.let { texts.add(it) }
            extractText(node.content)
        }
    }

    this.content?.forEach { node ->
        node.text?.let { texts.add(it) }
        extractText(node.content)
    }

    return texts.joinToString(" ")
}
