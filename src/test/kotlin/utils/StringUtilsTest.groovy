package utils

import spock.lang.Specification

class StringUtilsTest extends Specification {
    def "Assert that it defaults correctly"() {
        given:
          def underTest = new StringUtils()
        when:
          def actualResult = underTest.defaultWhenNull(input)
        then:
          actualResult == expectedResult
        where:
          input | expectedResult
          null  | "N/A"
          "foo" | "foo"
    }
}
