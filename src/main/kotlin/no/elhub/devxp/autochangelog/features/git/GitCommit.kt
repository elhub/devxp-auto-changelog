package no.elhub.devxp.autochangelog.features.git

import java.time.LocalDate
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class GitCommit(
    val hash: String,
    val title: String,
    val body: String,
    @Serializable(with = LocalDateSerializer::class)
    val date: LocalDate,
    val tags: List<GitTag>,
    val jiraIssues: List<String>
)

@Serializable
data class GitTag(
    val name: String,
    val commitHash: String,
)

object LocalDateSerializer : KSerializer<LocalDate> {
    override val descriptor = PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: LocalDate) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder) = LocalDate.parse(decoder.decodeString())
}
