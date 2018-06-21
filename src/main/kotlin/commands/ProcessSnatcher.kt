package commands

interface ProcessSnatcher {
    fun snatch(): String {
        return getCommandRunner().runCommand(getCommand())
    }

    fun getCommand(): String

    fun getCommandRunner() : CommandRunner {
        return ProcessBuilderCommandRunner()
    }
}