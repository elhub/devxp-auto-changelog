package no.elhub.devxp.autochangelog.git

/**
 * Represents a git message that consists of the [title] (first line in the commit message),
 * and a more thorough [description].
 */
data class GitMessage(
    val title: String,
    val description: List<String>
)

enum class TitleKeyword(val keywords: List<String>) {
    ADD(listOf("Add", "add", "Create", "create", "Implement", "implement")),
    BREAKING_CHANGE(listOf("Breaking Change", "breaking change")),
    CHANGE(listOf("Change", "change", "Deprecate", "deprecate", "Delete", "delete", "Refactor", "refactor", "Update", "update", "Remove", "remove", "feat")),
    FIX(listOf("Fix", "fix", "Bug", "bug", "BugFix", "bugfix")),
    RELEASE(listOf("Release", "release")),
    OTHER(emptyList()),
}

val GitMessage.titleKeyword: TitleKeyword
    get() = TitleKeyword.values().dropLast(1).firstOrNull {
        it.keywords.any { keyword -> title.startsWith(keyword, ignoreCase = true) }
    } ?: TitleKeyword.OTHER
