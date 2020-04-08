package api.cache

import api.definition.*

interface ICacheLoader {

    val frameIndex: Int

    fun loadSequences(library: ICacheLibrary): List<SequenceDefinition>

    fun loadNpcDefs(library: ICacheLibrary): Map<Int, NpcDefinition>

    fun loadItemDefs(library: ICacheLibrary): Map<Int, ItemDefinition>

    fun loadModelDef(library: ICacheLibrary, modelId: Int): ModelDefinition

    fun loadFrameArchive(library: ICacheLibrary, archiveId: Int): Set<FrameDefinition>
}