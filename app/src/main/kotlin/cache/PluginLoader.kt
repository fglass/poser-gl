package cache

import api.ICacheLoader
import api.ICachePacker
import java.io.File
import java.net.URLClassLoader
import java.util.ServiceLoader

object PluginLoader {

    fun load(): Pair<List<ICacheLoader>, List<ICachePacker>> {
        val path = File("../plugin")
        val jars = path.listFiles { _, name -> name.endsWith(".jar") }?: return Pair(emptyList(), emptyList())
        val urls = jars.map { it.toURI().toURL() }
        val classLoader = URLClassLoader.newInstance(urls.toTypedArray(), Thread.currentThread().contextClassLoader)

        val loaders = ServiceLoader.load(ICacheLoader::class.java, classLoader).asSequence().toList()
        val packers = ServiceLoader.load(ICachePacker::class.java, classLoader).asSequence().toList()
        classLoader.close()
        return Pair(loaders.sortedBy { it.toString() }, packers)
    }
}
