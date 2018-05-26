package utils

object Stopwatch {
    inline fun elapse(callback: () -> Unit): Long {
        val start = System.currentTimeMillis()
        callback()
        return System.currentTimeMillis() - start
    }
}