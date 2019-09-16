package cache.load

import cache.PluginProcessor
import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.multibindings.ProvidesIntoMap
import com.google.inject.multibindings.StringMapKey

class LoadModule: AbstractModule() {

    override fun configure() {
        bind(PluginProcessor::class.java)
    }

    @Provides
    fun provideOldSchoolPlugin(): CacheLoaderOSRS {
        return CacheLoaderOSRS()
    }

    @ProvidesIntoMap
    @StringMapKey("OSRS")
    fun getOldSchoolPlugin(plugin: CacheLoaderOSRS): ICacheLoader {
        return plugin
    }

    @Provides
    fun provide317Plugin(): CacheLoader317 {
        return CacheLoader317()
    }

    @ProvidesIntoMap
    @StringMapKey("317")
    fun get317Plugin(plugin: CacheLoader317): ICacheLoader {
        return plugin
    }

    @Provides
    fun provideLegacy317Plugin(): LegacyCacheLoader317 {
        return LegacyCacheLoader317()
    }

    @ProvidesIntoMap
    @StringMapKey("Legacy 317")
    fun getLegacyPlugin(plugin: LegacyCacheLoader317): ICacheLoader {
        return plugin
    }
}