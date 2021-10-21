package no.elhub.tools.autochangelog.git

/**
 * Represents a git message that consists of the [title] (first line in the commit message),
 * and a more thorough [description].
 */
data class GitMessage(
    val title: String,
    val description: List<String>
)
