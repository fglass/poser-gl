package cache

import load.ICacheLoader
import java.io.File
import java.net.URLClassLoader
import java.util.ServiceLoader

object PluginLoader {

    fun load(): List<ICacheLoader> {
        val path = File("./plugins")
        val jars = path.listFiles { _, name -> name.endsWith(".jar") }?: return emptyList()
        val urls = jars.map { it.toURI().toURL() }

        val classLoader = URLClassLoader.newInstance(urls.toTypedArray(), Thread.currentThread().contextClassLoader)
        val serviceLoader = ServiceLoader.load(ICacheLoader::class.java, classLoader)
        val plugins = serviceLoader.asSequence().toList()
        classLoader.close()
        return plugins
    }
}
