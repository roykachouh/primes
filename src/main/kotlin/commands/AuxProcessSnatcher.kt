package commands

import java.io.IOException
import java.util.*

const val COMMAND = "ps -eo pid,ppid,pcpu,pmem,comm"

// TODO abstract and generalize snatchers
class AuxProcessSnatcher {
    data class AuxProcess(val pid: String,
                          val ppid: String,
                          val cpuPercent: Double,
                          val memPercent: Double,
                          val command: String?
    )

    fun snatch(): String {
        return COMMAND.runCommand()!!
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
            println("Failed to snatch: "+ e.message)
            return null
        }
    }
}

