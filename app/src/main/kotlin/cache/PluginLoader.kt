package cache

import api.cache.ICacheLoader
import api.cache.ICachePacker
import java.io.File
import java.net.URLClassLoader
import java.util.ServiceLoader

object PluginLoader {

    fun load(): Pair<List<ICacheLoader>, List<ICachePacker>> {
        val jars = search("./plugin") + search("../plugin")
        if (jars.isEmpty()) {
            return Pair(emptyList(), emptyList())
        }

        val urls = jars.map { it.toURI().toURL() }
        val classLoader = URLClassLoader.newInstance(urls.toTypedArray(), Thread.currentThread().contextClassLoader)

        val loaders = ServiceLoader.load(ICacheLoader::class.java, classLoader).toList()
        val packers = ServiceLoader.load(ICachePacker::class.java, classLoader).toList()
        classLoader.close()
        return loaders.sortedBy { it.toString() } to packers
    }

    private fun search(path: String) = File(path).listFiles { _, name -> name.endsWith(".jar") } ?: emptyArray()
}
