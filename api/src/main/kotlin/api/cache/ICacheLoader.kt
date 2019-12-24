package api.cache

import api.definition.ItemDef
import api.definition.NpcDef
import api.definition.SequenceDef
import net.runelite.cache.definitions.*
import org.displee.CacheLibrary
import java.util.*

interface ICacheLoader {

    val frameIndex: Int

    fun loadSequences(library: CacheLibrary): List<SequenceDef>

    fun loadNpcDefs(library: CacheLibrary): HashMap<Int, NpcDef>

    fun loadItemDefs(library: CacheLibrary): HashMap<Int, ItemDef>

    fun loadModelDefinition(library: CacheLibrary, modelId: Int): ModelDefinition

    fun loadFrameArchive(library: CacheLibrary, archiveId: Int): Set<FrameDefinition>
}