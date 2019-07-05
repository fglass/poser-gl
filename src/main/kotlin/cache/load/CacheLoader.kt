package cache.load

import org.displee.CacheLibrary

interface CacheLoader {

    fun loadSequences(library: CacheLibrary)

    fun loadFrameArchive(archiveId: Int)

    fun loadNpcDefintions(library: CacheLibrary)

    fun loadItemDefinitions(library: CacheLibrary)
}