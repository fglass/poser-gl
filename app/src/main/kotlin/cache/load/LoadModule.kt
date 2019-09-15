package cache.load

import cache.PluginProcessor
import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.multibindings.ProvidesIntoMap
import com.google.inject.multibindings.StringMapKey
import render.RenderContext

class LoadModule: AbstractModule() {

    override fun configure() {
        bind(RenderContext::class.java)
        bind(PluginProcessor::class.java)
    }

    @Provides
    fun provideOldSchoolPlugin(context: RenderContext): CacheLoaderOSRS {
        return CacheLoaderOSRS(context)
    }

    @ProvidesIntoMap
    @StringMapKey("OSRS")
    fun getOldSchoolPlugin(plugin: CacheLoaderOSRS): ICacheLoader {
        return plugin
    }

    @Provides
    fun provide317Plugin(context: RenderContext): AltCacheLoader317 {
        return AltCacheLoader317(context)
    }

    @ProvidesIntoMap
    @StringMapKey("317")
    fun get317Plugin(plugin: AltCacheLoader317): ICacheLoader {
        return plugin
    }

    @Provides
    fun provideLegacy317Plugin(context: RenderContext): CacheLoader317 {
        return CacheLoader317(context)
    }

    @ProvidesIntoMap
    @StringMapKey("Legacy 317")
    fun getLegacyPlugin(plugin: CacheLoader317): ICacheLoader {
        return plugin
    }
}