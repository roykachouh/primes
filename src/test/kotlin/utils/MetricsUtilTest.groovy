package utils

import spock.lang.Specification

class MetricsUtilTest extends Specification {
    def "Assert that it cleans the namespace string correctly"() {
        given:
          def underTest = new MetricsUtil()
        when:
          def actualResult = underTest.sanitizeNamespace(input)
        then:
          actualResult == expectedResult
        where:
          input | expectedResult
          "model name      : Intel(R) Xeon(R) CPU E5-2670 v2 @ 2.50GHz"  | "IntelR XeonR CPU E52670 v2  250GHz"
    }
}
