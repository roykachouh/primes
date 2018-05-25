package benchmark

import kotlin.system.exitProcess

class PrimeTester(val numPrimes: String) {

    private val printPrimes = true

    fun run() {

        for (i in 3..numPrimes.toLong()) {
            var isPrime = true
            var j: Long = 2
            while (j <= i / 2 && isPrime) {
                isPrime = i % j > 0
                j++
            }
            if (isPrime && printPrimes) {
                println("Found prime: $i")
            }
        }
    }
}