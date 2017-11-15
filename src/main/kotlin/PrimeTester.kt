class PrimeTester : Thread() {
    override fun run() {
        while (true) {
            for (i in 3..10000) {
                var isPrime = true
                var j: Long = 2
                while (j <= i / 2 && isPrime) {
                    isPrime = i % j > 0
                    j++
                }
                if (isPrime) {
                    println("Found prime: " + i)
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun main(a: Array<String>) {
            var frequency: Long = 1

            val primeTester = PrimeTester()
            primeTester.start()
            while (true) {
                try {
                    Thread.sleep(frequency * 500)
                } catch (e: InterruptedException) {
                    println("Thread interrupted")
                }
            }
        }
    }
}