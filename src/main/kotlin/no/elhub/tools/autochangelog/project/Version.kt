package no.elhub.tools.autochangelog.project

import java.util.regex.Pattern

val versionPattern: Pattern = Pattern.compile("""(\d+)\.(\d+)\.(\d+)(?:-([a-zA-Z]+).(\d+))?""")

class Version : Comparable<Version> {
    val major: Int
    val minor: Int
    val patch: Int
    val preReleaseId: String?
    val preRelease: Int?

    constructor (maj: Int, min: Int, pat: Int, preRelId: String? = null, preRel: Int? = null) {
        major = maj
        minor = min
        patch = pat
        preReleaseId = preRelId
        preRelease = preRel
    }

    constructor (versionString: String) {
        val matcher = versionPattern.matcher(versionString)
        matcher.matches()
        major = matcher.group(1).toInt()
        minor = matcher.group(2).toInt()
        patch = matcher.group(3).toInt()
        preReleaseId = matcher.group(4)
        preRelease = matcher.group(5)?.toInt()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Version) return false
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

    override fun compareTo(other: Version): Int {
        when {
            major != other.major -> return major - other.major
            minor != other.minor -> return minor - other.minor
            patch != other.patch -> return patch - other.patch
            preReleaseId != other.preReleaseId -> {
                if (preReleaseId == null) return 1
                if (other.preReleaseId == null) return -1
                return preReleaseId.compareTo(other.preReleaseId)
            }
            preRelease != other.preRelease -> {
                if (preRelease == null) return 1
                if (other.preRelease == null) return -1
                return preRelease - other.preRelease
            }
            else -> return 0
        }
    }

    override fun toString(): String {
        return if (preReleaseId == null)
            "$major.$minor.$patch"
        else
            "$major.$minor.$patch-$preReleaseId.$preRelease"
    }
}
