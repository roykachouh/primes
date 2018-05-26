package metadata

import spock.lang.IgnoreIf
import spock.lang.Specification

class CPUMetadataSnatcherTest extends Specification {

    @IgnoreIf({ System.properties['os.name'] == 'Mac OS X' || System.properties['os.name'] == 'Windows' })
    def "Assert that processes mac cpuinfo"() {
        given:
          def underTest = new CPUMetadataSnatcher()
        when:
          def snatch = underTest.snatch()
        then:
          snatch instanceof CPUMetadataSnatcher.CpuMetadata
    }
}
