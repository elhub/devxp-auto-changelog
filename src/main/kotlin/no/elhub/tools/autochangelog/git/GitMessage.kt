package no.elhub.tools.autochangelog.git

/**
 * Represents a git message that consists of the [title] (first line in the commit message),
 * and a more thorough [description].
 */
data class GitMessage(
    val title: String,
    val description: List<String>
)

enum class TitleKeyword(val keywords: List<String>) {
    ADD(listOf("Add", "Create", "Implement")),
    BREAKING_CHANGE(listOf("Breaking Change")),
    CHANGE(listOf("Change", "Deprecate", "Delete", "Refactor", "Update", "Remove", "Update")),
    FIX(listOf("Fix")),
    OTHER(emptyList())
}

val GitMessage.titleKeyword: TitleKeyword
    get() = TitleKeyword.values().dropLast(1).firstOrNull {
        it.keywords.any { keyword -> title.startsWith(keyword) }
    } ?: TitleKeyword.OTHER
