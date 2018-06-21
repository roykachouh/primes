package commands

import spock.lang.Specification

class CPUVendorSnatcherTest extends Specification {

    def "Assert that processes mac cpuinfo"() {
        given:
          def underTest = new CPUVendorSnatcher()
        when:
          def snatch = underTest.snatch()
        then:
          snatch
    }
}
