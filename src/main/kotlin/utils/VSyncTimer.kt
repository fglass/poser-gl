package utils

class VSyncTimer {

    private val fps = 50f
    private var lastTime = System.currentTimeMillis()

    fun waitIfNecessary() {
        val now = System.currentTimeMillis()
        val current = now - lastTime
        lastTime = now

        val objective = (1000 / fps).toLong()
        val sleep = Math.max(0L, objective - current)
        Thread.sleep(sleep)
    }
}