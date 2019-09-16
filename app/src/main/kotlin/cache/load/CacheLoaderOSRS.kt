package cache.load

import cache.IndexType
import com.google.common.collect.HashMultimap
import net.runelite.cache.definitions.FrameDefinition
import net.runelite.cache.definitions.ItemDefinition
import net.runelite.cache.definitions.NpcDefinition
import net.runelite.cache.definitions.SequenceDefinition
import net.runelite.cache.definitions.loaders.*
import org.displee.CacheLibrary

class CacheLoaderOSRS: ICacheLoader {

    override fun loadSequences(library: CacheLibrary): List<SequenceDefinition> {
        val sequences = ArrayList<SequenceDefinition>()
        val sequenceLoader = SequenceLoader()
        val maxIndex = library.getIndex(IndexType.CONFIG.idOsrs)
            .getArchive(IndexType.SEQUENCE.idOsrs)
            .lastFile.id

        for (i in 0..maxIndex) {
            val file = library.getIndex(IndexType.CONFIG.idOsrs)
                .getArchive(IndexType.SEQUENCE.idOsrs)
                .getFile(i) ?: continue

            sequences.add(sequenceLoader.load(file.id, file.data))
        }
        return sequences
    }

    override fun loadFrameArchives(library: CacheLibrary): HashMultimap<Int, FrameDefinition> {
        val frames: HashMultimap<Int, FrameDefinition> = HashMultimap.create()
        val frameIndex = IndexType.FRAME.idOsrs
        val frameLoader = FrameLoader()
        val frameMapLoader = FramemapLoader()
        val frameMapIndex = IndexType.FRAME_MAP.idOsrs

        for (i in 0..library.getIndex(frameIndex).lastArchive.id) {
            for (j in 0..library.getIndex(frameIndex).getArchive(i).lastFile.id) {
                val frameFile = library.getIndex(frameIndex).getArchive(i).getFile(j) ?: continue
                val frameData = frameFile.data

                val frameMapArchiveId = (frameData[0].toInt() and 0xff) shl 8 or (frameData[1].toInt() and 0xff)
                val frameMapFile = library.getIndex(frameMapIndex).getArchive(frameMapArchiveId).getFile(0)

                val frameMap = frameMapLoader.load(frameMapArchiveId, frameMapFile.data)
                val frame = frameLoader.load(frameMap, frameFile.id, frameData)
                frames.put(i, frame)
            }
        }
        return frames
    }

    override fun loadNpcDefinitions(library: CacheLibrary): HashMap<Int, NpcDefinition> {
        val entities = HashMap<Int, NpcDefinition>()
        val npcLoader = NpcLoader()
        val maxIndex = library.getIndex(IndexType.CONFIG.idOsrs)
            .getArchive(IndexType.NPC.idOsrs)
            .lastFile.id

        for (i in 0..maxIndex) {
            val file = library.getIndex(IndexType.CONFIG.idOsrs)
                .getArchive(IndexType.NPC.idOsrs)
                .getFile(i)?: continue

            val npc = npcLoader.load(file.id, file.data)
            if (npc.models != null && npc.name.toLowerCase() != "null" && npc.name != "") {
                entities[npc.id] = npc
            }
        }
        return entities
    }

    override fun loadItemDefinitions(library: CacheLibrary): HashMap<Int, ItemDefinition> {
        val items = HashMap<Int, ItemDefinition>()
        val itemLoader = ItemLoader()
        val maxIndex = library.getIndex(IndexType.CONFIG.idOsrs)
            .getArchive(IndexType.ITEM.idOsrs)
            .lastFile.id

        for (i in 0..maxIndex) {
            val file = library.getIndex(IndexType.CONFIG.idOsrs)
                .getArchive(IndexType.ITEM.idOsrs)
                .getFile(i)?: continue

            val item = itemLoader.load(file.id, file.data)
            if (item.maleModel0 > 0 && item.name.toLowerCase() != "null") {
                items[item.id] = item
            }
        }
        return items
    }
}