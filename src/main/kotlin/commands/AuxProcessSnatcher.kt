package commands

import java.io.IOException
import java.util.*


class AuxProcessSnatcher {
    data class AuxProcess(val pid: String?,
                          val ppid: String?,
                          val cpuPercent: String?,
                          val memPercent: String?,
                          val command: String?
    )

    fun snatch(): List<AuxProcess>? {

        val lines = "ps -eo pid,ppid,pcpu,pmem,comm".runCommand()!!.split("\n")

        return lines
                .filter {
                    it.isNotEmpty()
                }.map { line: String ->
                    val cleansed = line.trim().replace("\\s+".toRegex(), ",")
                    val fields = cleansed.split(",")
                    println(line)
                    AuxProcess(
                            pid = fields[0],
                            ppid = fields[1],
                            cpuPercent = fields[2],
                            memPercent = fields[3],
                            command = fields[4]
                    )
                }
    }

    private fun String.runCommand(): String? {
        try {
            val commands = ArrayList<String>()
            commands.add("/bin/sh")
            commands.add("-c")
            commands.add(this)

            val commandExecutor = ProcessBuilder()
            commandExecutor.command(commands)

            val result = commandExecutor.start()

            return result.inputStream.bufferedReader().readText()
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }
}

