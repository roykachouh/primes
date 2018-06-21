package commands

import java.io.IOException
import java.util.*

class ProcessBuilderCommandRunner : CommandRunner {

    override fun runCommand(command: String): String {
        return command.runCommandInternal()
    }

    private fun String.runCommandInternal(): String {
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
            throw RuntimeException(e)
        }
    }

}