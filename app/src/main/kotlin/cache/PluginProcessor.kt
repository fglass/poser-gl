package cache

import cache.load.ICacheLoader
import com.google.inject.Inject
import com.google.inject.Singleton

@Singleton
class PluginProcessor @Inject constructor(private val plugins: Map<String, @JvmSuppressWildcards ICacheLoader>){

    fun getPlugin(key: String): ICacheLoader? {
        return plugins[key]
    }
}