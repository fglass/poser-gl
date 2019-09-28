import com.google.common.collect.HashMultimap
import api.ICacheLoader
import net.runelite.cache.definitions.FrameDefinition
import net.runelite.cache.definitions.ItemDefinition
import net.runelite.cache.definitions.NpcDefinition
import net.runelite.cache.definitions.SequenceDefinition
import net.runelite.cache.definitions.loaders.*
import org.displee.CacheLibrary

const val FRAME_INDEX = 0
const val FRAME_MAP_INDEX = 1
const val CONFIG_INDEX = 2
const val NPC_INDEX = 9
const val ITEM_INDEX = 10
const val SEQUENCE_INDEX = 12

class CacheLoaderOSRS: ICacheLoader {

    override fun toString() = "OSRS"

    override fun loadSequences(library: CacheLibrary): List<SequenceDefinition> {
        val sequences = ArrayList<SequenceDefinition>()
        val sequenceLoader = SequenceLoader()
        val maxIndex = library.getIndex(CONFIG_INDEX).getArchive(SEQUENCE_INDEX).lastFile.id

        for (i in 0..maxIndex) {
            val file = library.getIndex(CONFIG_INDEX).getArchive(SEQUENCE_INDEX).getFile(i) ?: continue
            sequences.add(sequenceLoader.load(file.id, file.data))
        }
        return sequences
    }

    override fun loadFrameArchives(library: CacheLibrary): HashMultimap<Int, FrameDefinition> {
        val frames: HashMultimap<Int, FrameDefinition> = HashMultimap.create()
        val frameLoader = FrameLoader()
        val frameMapLoader = FramemapLoader()

        for (i in 0..library.getIndex(FRAME_INDEX).lastArchive.id) {
            for (j in 0..library.getIndex(FRAME_INDEX).getArchive(i).lastFile.id) {
                val frameFile = library.getIndex(FRAME_INDEX).getArchive(i).getFile(j) ?: continue
                val frameData = frameFile.data

                val frameMapArchiveId = (frameData[0].toInt() and 0xff) shl 8 or (frameData[1].toInt() and 0xff)
                val frameMapFile = library.getIndex(FRAME_MAP_INDEX).getArchive(frameMapArchiveId).getFile(0)

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
        val maxIndex = library.getIndex(CONFIG_INDEX).getArchive(NPC_INDEX).lastFile.id

        for (i in 0..maxIndex) {
            val file = library.getIndex(CONFIG_INDEX).getArchive(NPC_INDEX).getFile(i)?: continue
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
        val maxIndex = library.getIndex(CONFIG_INDEX).getArchive(ITEM_INDEX).lastFile.id

        for (i in 0..maxIndex) {
            val file = library.getIndex(CONFIG_INDEX).getArchive(ITEM_INDEX).getFile(i)?: continue
            val item = itemLoader.load(file.id, file.data)
            if (item.maleModel0 > 0 && item.name.toLowerCase() != "null") {
                items[item.id] = item
            }
        }
        return items
    }
}