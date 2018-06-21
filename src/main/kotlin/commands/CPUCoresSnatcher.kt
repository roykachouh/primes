package commands


class CPUCoresSnatcher : ProcessSnatcher {
    override fun getCommand(): String {
        return "cat /proc/cpuinfo | grep processor | wc -l"
    }
}

