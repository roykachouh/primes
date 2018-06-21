package commands


class CPUVendorSnatcher : ProcessSnatcher {
    override fun getCommand(): String {
        return "cat /proc/cpuinfo | grep 'vendor' | uniq"
    }
}

