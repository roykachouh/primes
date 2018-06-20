package commands

import spock.lang.Specification

class AuxProcessSnatcherTest extends Specification {

    def "Assert that snatch returns process tree"() {
        given:
          def underTest = new AuxProcessSnatcher()
        when:
          def parsedProcesses = underTest.snatch()
        then: "There should be at least one parsed process with fields filled out"
          parsedProcesses.size() > 1
          parsedProcesses.first().command
          parsedProcesses.first().cpuPercent
          parsedProcesses.first().memPercent
          parsedProcesses.first().pid
          parsedProcesses.first().ppid
    }
}
