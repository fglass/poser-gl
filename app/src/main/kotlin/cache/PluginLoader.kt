package cache

import api.ICacheLoader
import java.io.File
import java.net.URLClassLoader
import java.util.ServiceLoader

object PluginLoader {

    fun getLoaders(): List<ICacheLoader> {
        val path = File("./plugins")
        val jars = path.listFiles { _, name -> name.endsWith(".jar") }?: return emptyList()
        val urls = jars.map { it.toURI().toURL() }

        val classLoader = URLClassLoader.newInstance(urls.toTypedArray(), Thread.currentThread().contextClassLoader)
        val serviceLoader = ServiceLoader.load(ICacheLoader::class.java, classLoader)
        val loaders = serviceLoader.asSequence().toList()
        classLoader.close()
        return loaders.sortedBy { it.toString() }
    }
}
