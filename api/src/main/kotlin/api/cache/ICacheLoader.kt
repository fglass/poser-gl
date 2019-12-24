package api.cache

import api.definition.*
import org.displee.CacheLibrary
import java.util.*

interface ICacheLoader {

    val frameIndex: Int

    fun loadSequences(library: CacheLibrary): List<SequenceDef>

    fun loadNpcDefs(library: CacheLibrary): HashMap<Int, NpcDef>

    fun loadItemDefs(library: CacheLibrary): HashMap<Int, ItemDef>

    fun loadModelDef(library: CacheLibrary, modelId: Int): ModelDef

    fun loadFrameArchive(library: CacheLibrary, archiveId: Int): Set<FrameDef>
}