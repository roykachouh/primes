package commands

interface CommandRunner {
    fun runCommand(command: String): String
}