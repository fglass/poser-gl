package cache

import cache.load.ICacheLoader
import org.displee.CacheLibrary
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import java.util.ServiceLoader

object PluginLoader {

    /*fun getServiceLoader(): ServiceLoader<ICacheLoader>? {
        if (serviceLoader == null) {
            loadPlugins()
        }
        return serviceLoader
    }*/

    fun load(library: CacheLibrary) { // TODO: refactor
        val pluginPath = File("/Users/fred/Documents/PoserGL/app/plugins")
        val plugins = pluginPath.listFiles { _, name -> name.endsWith(".jar") }?: return
        val urls = ArrayList<URL>()

        for (file in plugins) {
            val url = file.toURI().toURL()
            urls.add(url)
        }

        val classLoader = URLClassLoader.newInstance(urls.toTypedArray(), Thread.currentThread().contextClassLoader)
        val serviceLoader = ServiceLoader.load(ICacheLoader::class.java, classLoader)

        var count = 0
        for (plugin in serviceLoader) {
            plugin.loadSequences(library)
            count++
        }
        println("Loaded $count plugin(s)")
        classLoader.close()
    }
}
