package utils

object StringUtils {
    val notApplicable = "N/A"

    inline fun defaultWhenNull(input: String?, default: String): String {
        return input ?: default
    }

    inline fun defaultWhenNull(input: String?): String {
        return defaultWhenNull(input, notApplicable)
    }
}