package util

import kotlin.math.max

private const val TARGET_FPS = 50f

class VSyncTimer {

    private var lastTime = System.currentTimeMillis()

    fun waitIfNecessary() {
        val now = System.currentTimeMillis()
        val current = now - lastTime
        lastTime = now

        val objective = (1000 / TARGET_FPS).toLong()
        val sleep = max(0L, objective - current)
        Thread.sleep(sleep)
    }
}