package api.cache

import api.definition.*
import org.displee.CacheLibrary
import java.util.*

interface ICacheLoader {

    val frameIndex: Int

    fun loadSequences(library: CacheLibrary): List<SequenceDefinition>

    fun loadNpcDefs(library: CacheLibrary): HashMap<Int, NpcDefinition>

    fun loadItemDefs(library: CacheLibrary): HashMap<Int, ItemDefinition>

    fun loadModelDef(library: CacheLibrary, modelId: Int): ModelDefinition

    fun loadFrameArchive(library: CacheLibrary, archiveId: Int): Set<FrameDefinition>
}