package commands

import spock.lang.Specification

class AuxProcessSnatcherTest extends Specification {

    def "Assert that snatch returns process tree"() {
        given:
          def underTest = new AuxProcessSnatcher()
        when:
          def parsedProcessString = underTest.snatch()
        then:
          parsedProcessString
    }
}
