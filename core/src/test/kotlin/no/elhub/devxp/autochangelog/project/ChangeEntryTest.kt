package no.elhub.devxp.autochangelog.project

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import no.elhub.devxp.autochangelog.git.GitMessage
import no.elhub.devxp.autochangelog.git.TitleKeyword

class ChangeEntryTest : FunSpec({
    context("ChangeEntry.Builder") {
        context("GitMessage") {
            TitleKeyword.values().forEach {
                test("should add entry to correct collection $it title keyword") {
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
                        TitleKeyword.RELEASE -> {}
                        TitleKeyword.OTHER -> builder.other shouldHaveSize 1
                    }
                }
            }
        }
    }
})
