package no.elhub.devxp.autochangelog.jira

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import no.elhub.devxp.autochangelog.jiraIssueExtractor
import no.elhub.devxp.autochangelog.project.ChangelogEntry
import no.elhub.devxp.autochangelog.project.SemanticVersion
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import java.time.LocalDate

class JiraChangelogEntryTest : FunSpec({

    fun startKoin(): JiraIssueExtractor {
        val jiraIssueExtractor = jiraIssueExtractor()
        startKoin {
            modules(
                module {
                    single {
                        jiraIssueExtractor
                    }
                }
            )
        }
        return jiraIssueExtractor
    }

    context("JiraChangelogEntry") {

        test("serializes and deserializes correctly") {
            val entry = JiraChangelogEntry(
                release = JiraChangelogEntry.Release(SemanticVersion("1.0.0"), LocalDate.of(2024, 6, 1)),
                added = listOf(
                    JiraChangelogEntry.JiraEntry(
                        "Added feature",
                        listOf(
                            JiraChangelogEntry.JiraIssueInfo("PROJ-1", "Title", "Desc", "http://jira/PROJ-1")
                        )
                    )
                )
            )
            val json = Json.encodeToString(JiraChangelogEntry.serializer(), entry)
            val decoded = Json.decodeFromString(JiraChangelogEntry.serializer(), json)
            decoded shouldBe entry
        }

        test("fromChangelogEntry returns simple JiraChangelogEntry when jiraEnabled is false") {
            startKoin().initialize("user", "token")
            val changelogEntry = ChangelogEntry(
                release = ChangelogEntry.Release(SemanticVersion("1.0.0"), LocalDate.of(2024, 6, 1)),
                added = listOf("Added feature"),
                changed = listOf("Changed something"),
                fixed = listOf("Fixed bug"),
                breakingChange = listOf("Breaking change"),
                other = listOf("Other note")
            )
            val result = JiraChangelogEntry.fromChangelogEntry(changelogEntry, jiraEnabled = false)
            result.added[0].text shouldBe "Added feature"
            result.added[0].jira_issues shouldBe emptyList()
            stopKoin()
        }

        test("fromChangelogEntry returns JiraChangelogEntry with Jira issues when jiraEnabled is true and extractor initialized") {
            startKoin().initialize("user", "token")
            val changelogEntry = ChangelogEntry(
                release = ChangelogEntry.Release(SemanticVersion("1.0.0"), LocalDate.of(2024, 6, 1)),
                added = listOf("Added PROJ-1"),
                changed = emptyList(),
                fixed = emptyList(),
                breakingChange = emptyList(),
                other = emptyList()
            )
            val result = JiraChangelogEntry.fromChangelogEntry(changelogEntry, jiraEnabled = true)
            result.added[0].jira_issues shouldBe listOf(
                JiraChangelogEntry.JiraIssueInfo(
                    "PROJ-1",
                    "Title",
                    "This is a description.",
                    "https://elhub.atlassian.net/browse/PROJ-1"
                )
            )
            stopKoin()
        }
    }
})
