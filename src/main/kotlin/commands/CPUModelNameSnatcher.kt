package commands


class CPUModelNameSnatcher : ProcessSnatcher {
    override fun getCommand(): String {
        return "cat /proc/cpuinfo | grep 'model name' | uniq"
    }
}