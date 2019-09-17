package load

import cache.load.ICacheLoader
import com.google.common.collect.HashMultimap
import net.runelite.cache.definitions.FrameDefinition
import net.runelite.cache.definitions.ItemDefinition
import net.runelite.cache.definitions.NpcDefinition
import net.runelite.cache.definitions.SequenceDefinition
import org.displee.CacheLibrary

class TestLoader: ICacheLoader { // TODO: refactor pom

    override fun loadSequences(library: CacheLibrary): List<SequenceDefinition> {
        println("test1")
        return ArrayList()
    }

    override fun loadFrameArchives(library: CacheLibrary): HashMultimap<Int, FrameDefinition> {
        println("test2")
        return HashMultimap.create()
    }

    override fun loadNpcDefinitions(library: CacheLibrary): HashMap<Int, NpcDefinition> {
        println("test3")
        return HashMap()
    }

    override fun loadItemDefinitions(library: CacheLibrary): HashMap<Int, ItemDefinition> {
        println("test4")
        return HashMap()
    }
}