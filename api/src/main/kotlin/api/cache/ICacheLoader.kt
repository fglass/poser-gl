package api.cache

import api.definition.ItemDef
import net.runelite.cache.definitions.*
import org.displee.CacheLibrary
import java.util.*

interface ICacheLoader {

    val frameIndex: Int

    fun loadSequences(library: CacheLibrary): List<SequenceDefinition>

    fun loadNpcDefinitions(library: CacheLibrary): HashMap<Int, NpcDefinition>

    fun loadItemDefs(library: CacheLibrary): HashMap<Int, ItemDef>

    fun loadModelDefinition(library: CacheLibrary, modelId: Int): ModelDefinition

    fun loadFrameArchive(library: CacheLibrary, archiveId: Int): Set<FrameDefinition>
}