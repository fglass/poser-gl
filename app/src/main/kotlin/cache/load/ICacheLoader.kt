package cache.load

import animation.Animation
import com.google.common.collect.HashMultimap
import net.runelite.cache.definitions.FrameDefinition
import net.runelite.cache.definitions.ItemDefinition
import net.runelite.cache.definitions.NpcDefinition
import net.runelite.cache.definitions.SequenceDefinition
import org.displee.CacheLibrary

interface ICacheLoader {

    fun loadSequences(library: CacheLibrary): List<SequenceDefinition>

    fun loadFrameArchives(library: CacheLibrary): HashMultimap<Int, FrameDefinition>

    fun loadNpcDefinitions(library: CacheLibrary): HashMap<Int, NpcDefinition>

    fun loadItemDefinitions(library: CacheLibrary): HashMap<Int, ItemDefinition>
}