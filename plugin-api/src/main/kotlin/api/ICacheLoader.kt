package api

import com.google.common.collect.HashMultimap
import net.runelite.cache.definitions.*
import org.displee.CacheLibrary
import java.util.*

interface ICacheLoader {

    fun loadSequences(library: CacheLibrary): List<SequenceDefinition>

    fun loadFrameArchives(library: CacheLibrary): HashMultimap<Int, FrameDefinition>

    fun loadNpcDefinitions(library: CacheLibrary): HashMap<Int, NpcDefinition>

    fun loadItemDefinitions(library: CacheLibrary): HashMap<Int, ItemDefinition>

    fun loadModelDefinition(library: CacheLibrary, modelId: Int): ModelDefinition

}