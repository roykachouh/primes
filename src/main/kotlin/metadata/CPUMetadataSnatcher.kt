package metadata

import java.io.IOException
import java.util.concurrent.TimeUnit

class CPUMetadataSnatcher {
    data class CpuMetadata(val vendor: String?,
                           val modelName: String?,
                           val cores: String?
    )

    fun snatch(): CpuMetadata {
        return CpuMetadata(
                vendor = "cat /proc/cpuinfo | grep 'vendor' | uniq".runCommand(),
                modelName = "cat /proc/cpuinfo | grep 'model name' | uniq".runCommand(),
                cores = "cat /proc/cpuinfo | grep processor | wc -l".runCommand()
        )
    }

    fun String.runCommand(): String? {
        try {
            val parts = this.split("\\s".toRegex())
            val proc = ProcessBuilder(*parts.toTypedArray())
                    .redirectOutput(ProcessBuilder.Redirect.PIPE)
                    .redirectError(ProcessBuilder.Redirect.PIPE)
                    .start()

            proc.waitFor(60, TimeUnit.SECONDS)

            val stdout = proc.inputStream.bufferedReader().readText()

            if (stdout.isBlank()) {
                return null
            }
            return stdout
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }
}

