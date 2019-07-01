package cache

import CACHE_317_PATH
import CACHE_PATH
import Processor
import animation.Animation
import com.google.common.collect.HashMultimap
import net.runelite.cache.definitions.FrameDefinition
import net.runelite.cache.definitions.ItemDefinition
import net.runelite.cache.definitions.NpcDefinition
import net.runelite.cache.definitions.loaders.*
import org.displee.CacheLibrary

class CacheService(private val context: Processor) {

    val entities = HashMap<Int, NpcDefinition>()
    val items = HashMap<Int, ItemDefinition>()
    var animations = HashMap<Int, Animation>()
    val frames: HashMultimap<Int, FrameDefinition> = HashMultimap.create()

    init {
        val library = CacheLibrary(CACHE_PATH)
        loadEntities(library)
        loadItems(library)
        loadSequences(library)
        loadFrames(library)
        library.close()
    }

    private fun loadEntities(library: CacheLibrary) {
        val npcLoader = NpcLoader()
        for (i in 0..library.getIndex(IndexType.CONFIG.id).getArchive(ConfigType.NPC.id).lastFile.id) {
            val file = library.getIndex(IndexType.CONFIG.id).getArchive(ConfigType.NPC.id).getFile(i)?: continue
            val npc = npcLoader.load(file.id, file.data)
            if (npc.models != null && npc.name.toLowerCase() != "null") {
                entities[npc.id] = npc
            }
        }
        addPlayer()
        println("Loaded ${entities.size} entities")
    }

    private fun addPlayer() {
        val player = NpcDefinition(0)
        player.name = "Player"
        player.models = intArrayOf(230, 249, 292, 151, 176, 254, 181)
        entities[player.id] = player
    }


    private fun loadItems(library: CacheLibrary) {
        val itemLoader = ItemLoader()
        for (i in 0..library.getIndex(IndexType.CONFIG.id).getArchive(ConfigType.ITEM.id).lastFile.id) {
            val file = library.getIndex(IndexType.CONFIG.id).getArchive(ConfigType.ITEM.id).getFile(i)?: continue
            val item = itemLoader.load(file.id, file.data)
            if (item.maleModel0 != -1 && item.name.toLowerCase() != "null") {
                items[item.id] = item
            }
        }
        println("Loaded ${items.size} items")
    }

    private fun loadSequences(library: CacheLibrary) {
        /*val lib = CacheLibrary(CACHE_317_PATH)
        val sequenceArchive = lib.getIndex(0).getArchive(2).getFile("seq.dat") // seq.dat hash: 886159288
        animations = SequenceLoader317().load(context, sequenceArchive.data)
        lib.close()*/

        val sequenceLoader = SequenceLoader()
        for (i in 0..library.getIndex(IndexType.CONFIG.id).getArchive(ConfigType.SEQUENCE.id).lastFile.id) {
            val file = library.getIndex(IndexType.CONFIG.id).getArchive(ConfigType.SEQUENCE.id).getFile(i)?: continue
            val sequence = sequenceLoader.load(file.id, file.data)
            val animation = Animation(context, sequence)
            animations[file.id] = animation
        }
        println("Loaded ${animations.size} sequences")
    }

    private fun loadFrames(library: CacheLibrary) {
        val frameLoader = FrameLoader()
        val frameMapLoader = FramemapLoader()

        for (i in 0..library.getIndex(IndexType.FRAME.id).lastArchive.id) {
            val archive = library.getIndex(IndexType.FRAME.id).getArchive(i)?: continue

            for (j in 0..library.getIndex(IndexType.FRAME.id).getArchive(i).lastFile.id) {
                val frameFile = library.getIndex(IndexType.FRAME.id).getArchive(i).getFile(j)?: continue
                val frameData = frameFile.data

                val frameMapArchiveId = (frameData[0].toInt() and 0xff) shl 8 or (frameData[1].toInt() and 0xff)
                val frameMapFile = library.getIndex(IndexType.FRAME_MAP.id).getArchive(frameMapArchiveId).getFile(0)

                val frameMap = frameMapLoader.load(frameMapArchiveId, frameMapFile.data)
                val frame = frameLoader.load(frameMap, frameFile.id, frameData)
                frames.put(archive.id, frame)
            }
        }
        println("Loaded ${frames.size()} frames")
    }

    fun pack(animation: Animation) {
        println("Packing ${animation.sequence.id}")
        val library = CacheLibrary(CACHE_PATH)

        val maxArchiveId = frames.keySet().max()!!
        val newArchiveId = maxArchiveId + 1

        library.getIndex(IndexType.FRAME.id).addArchive(newArchiveId)

        for (keyframe in animation.keyframes) {
            val bytes = keyframe.encode()
            library.getIndex(IndexType.FRAME.id).getArchive(newArchiveId).addFile(keyframe.id, bytes)
        }

        library.getIndex(IndexType.FRAME.id).update()
        println("Packed frames")

        val sequence = animation.toSequence()
        val bytes = animation.encode(sequence)

        library.getIndex(IndexType.FRAME.id).getArchive(ConfigType.SEQUENCE.id).addFile(sequence.id, bytes)
        library.getIndex(IndexType.FRAME.id).update()
        println("Packed ${animation.sequence.id}")
        library.close()
    }
}