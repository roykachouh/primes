package commands

class AuxProcessSnatcher : ProcessSnatcher {
    override fun getCommand(): String {
        return "ps -eo pid,ppid,pcpu,pmem,comm"
    }
}

