package no.elhub.tools.autochangelog.project

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import no.elhub.tools.autochangelog.config.Configuration.jiraIssuesUrl
import no.elhub.tools.autochangelog.git.GitMessage
import no.elhub.tools.autochangelog.git.TitleKeyword

class ChangeEntryTest : DescribeSpec({
    describe("ChangeEntry.Builder") {
        context("GitMessage") {
            TitleKeyword.values().forEach {
                it("should add entry to correct collection $it title keyword") {
                    val builder = ChangelogEntry.Builder()
                    val msg = GitMessage(
                        title = "${it.keywords.ifEmpty { listOf("Unknown") }.first()} test commit",
                        description = listOf("JIRA Issues: TD-42")
                    )
                    builder.withMessage(msg)

                    when (it) {
                        TitleKeyword.ADD -> builder.added shouldHaveSize 1
                        TitleKeyword.BREAKING_CHANGE -> builder.breakingChange shouldHaveSize 1
                        TitleKeyword.CHANGE -> builder.changed shouldHaveSize 1
                        TitleKeyword.FIX -> builder.fixed shouldHaveSize 1
                        TitleKeyword.OTHER -> builder.other shouldHaveSize 1
                    }
                }
            }

            it("should add url for jira issues") {
                val builder = ChangelogEntry.Builder()
                val msg = GitMessage(
                    "Add test commit",
                    listOf("JIRA Issues: TD-1")
                )
                builder.withMessage(msg)
                builder.added shouldHaveSize 1
                builder.added.first() shouldBe "[ [TD-1]($jiraIssuesUrl/TD-1) ] Add test commit"
            }

            it("should add urls for jira issues") {
                val builder = ChangelogEntry.Builder()
                val msg = GitMessage(
                    "Add test commit",
                    listOf("JIRA Issues: TD-1, TD-2, TD-3")
                )
                builder.withMessage(msg)
                builder.added shouldHaveSize 1
                builder.added.first() shouldBe """
                    [ [TD-1]($jiraIssuesUrl/TD-1), [TD-2]($jiraIssuesUrl/TD-2), [TD-3]($jiraIssuesUrl/TD-3) ] Add test commit
                """.trimIndent()
            }
        }
    }
})
