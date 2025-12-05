package no.elhub.devxp.autochangelog.project

import kotlinx.serialization.Serializable
import java.util.regex.Pattern

val versionPattern: Pattern = Pattern.compile("""^(\d+)\.(\d+)\.(\d+)(?:-([a-zA-Z]+).(\d+))?""")

@Serializable
sealed interface Version

@Serializable
class SemanticVersion : Comparable<SemanticVersion>, Version {
    val major: Int
    val minor: Int
    val patch: Int
    val preReleaseId: String?
    val preRelease: Int?

    constructor (major: Int, minor: Int, patch: Int, preReleaseId: String? = null, preRelease: Int? = null) {
        this.major = major
        this.minor = minor
        this.patch = patch
        this.preReleaseId = preReleaseId
        this.preRelease = preRelease
    }

    constructor (versionString: String) {
        val matcher = versionPattern.matcher(versionString)
        require(matcher.matches()) { "Version '$versionString' is not compliant with semantic release rules" }
        major = matcher.group(1).toInt()
        minor = matcher.group(2).toInt()
        patch = matcher.group(3).toInt()
        preReleaseId = matcher.group(4)
        preRelease = matcher.group(5)?.toInt()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is SemanticVersion) return false
        return major == other.major &&
            minor == other.minor &&
            patch == other.patch &&
            preReleaseId == other.preReleaseId &&
            preRelease == other.preRelease
    }

    // Always override hashcode when changing equals
    override fun hashCode(): Int {
        var result = major
        result = 31 * result + minor
        result = 31 * result + patch
        result = 31 * result + (preReleaseId?.hashCode() ?: 0)
        result = 31 * result + (preRelease ?: 0)
        return result
    }

    override fun compareTo(other: SemanticVersion): Int {
        return when {
            major != other.major -> return major - other.major

            minor != other.minor -> return minor - other.minor

            patch != other.patch -> return patch - other.patch

            preReleaseId != other.preReleaseId -> {
                if (preReleaseId == null) return 1
                if (other.preReleaseId == null) return -1
                preReleaseId.compareTo(other.preReleaseId)
            }

            preRelease != other.preRelease -> {
                if (preRelease == null) return 1
                if (other.preRelease == null) return -1
                preRelease - other.preRelease
            }

            else -> 0
        }
    }

    override fun toString(): String = if (preReleaseId == null) {
        "$major.$minor.$patch"
    } else {
        "$major.$minor.$patch-$preReleaseId.$preRelease"
    }
}

@Serializable
object Unreleased : Version {
    override fun toString() = "UNRELEASED"
}
