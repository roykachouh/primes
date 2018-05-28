package utils

object MetricsUtil {
    val alphanumbericRegex = Regex("[^A-Za-z0-9 ]")

    inline fun sanitizeNamespace(input: String?): String? {


        val clean = input
                ?.substring(input.indexOf(":") + 1)
                ?.trim()


        return alphanumbericRegex.replace(clean!!, "")
    }
}