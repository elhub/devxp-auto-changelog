package no.elhub.devxp.autochangelog.features.git

import java.time.LocalDate
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDateTime

@Serializable
data class GitCommit(
    val hash: String,
    val title: String,
    val body: String,
    @Serializable(with = LocalDateSerializer::class)
    val commitTime: LocalDateTime,
    val tags: List<GitTag>,
    val jiraIssues: List<String>
)

@Serializable
data class GitTag(
    val name: String,
    val commitHash: String,
)

// We serialize LocalDateTime as LocalDate because we don't care about the time component in this context
object LocalDateSerializer : KSerializer<LocalDateTime> {
    override val descriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDateTime) =
        encoder.encodeString(value.toLocalDate().toString())

    override fun deserialize(decoder: Decoder) =
        LocalDate.parse(decoder.decodeString()).atStartOfDay()
}
