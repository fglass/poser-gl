package cache

import api.TestService
import java.util.*

object PluginLoader {

    fun run() {
        val loader = ServiceLoader.load(TestService::class.java)
        val iterator = loader.iterator()

        while (iterator.hasNext()) {
            val next = iterator.next()
            println(next.getAccountId())
        }
    }
}