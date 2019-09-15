package cache.load

import animation.Animation
import net.runelite.cache.definitions.FrameDefinition
import net.runelite.cache.definitions.ItemDefinition
import net.runelite.cache.definitions.NpcDefinition
import org.displee.CacheLibrary

interface ICacheLoader { // TODO: map/set instead of impl's

    fun loadSequences(library: CacheLibrary): HashMap<Int, Animation>

    fun loadFrameArchive(archiveId: Int, library: CacheLibrary): HashSet<FrameDefinition>

    fun loadNpcDefintions(library: CacheLibrary): HashMap<Int, NpcDefinition>

    fun loadItemDefinitions(library: CacheLibrary): HashMap<Int, ItemDefinition>
}