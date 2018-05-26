package metadata

import java.io.IOException
import java.util.*
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

    private fun String.runCommand(): String? {
        try {
            val commands = ArrayList<String>()
            commands.add("/bin/sh")
            commands.add("-c")
            commands.add(this)

            val commandExecutor = ProcessBuilder()
            commandExecutor.command(commands)

            val result = commandExecutor.redirectOutput(ProcessBuilder.Redirect.PIPE)
                    .redirectError(ProcessBuilder.Redirect.PIPE)
                    .start()

            result.waitFor(60, TimeUnit.SECONDS)

            return result.inputStream.bufferedReader().readText().replace("\n","")
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }
}

