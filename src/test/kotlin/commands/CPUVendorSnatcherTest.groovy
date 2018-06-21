package commands

import spock.lang.Specification

class CPUVendorSnatcherTest extends Specification {

    def "Assert that processes mac cpuinfo"() {
        given:
          def commandRunner = Mock(CommandRunner)
          commandRunner.runCommand(_) >> "vendor_id       : GenuineIntel"
          def underTest = new CPUVendorSnatcher(commandRunner)
        when:
          def snatch = underTest.snatch()
        then:
          snatch
    }
}
