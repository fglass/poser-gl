package cache

import CACHE_PATH
import Processor
import animation.Animation
import com.google.common.collect.HashMultimap
import net.runelite.cache.definitions.FrameDefinition
import net.runelite.cache.definitions.ItemDefinition
import net.runelite.cache.definitions.NpcDefinition
import net.runelite.cache.definitions.loaders.*
import org.displee.CacheLibrary

class CacheService(private val context: Processor) { // TODO: Clean-up this, loader & buffer

    private val osrs: Boolean
    val entities = HashMap<Int, NpcDefinition>()
    val items = HashMap<Int, ItemDefinition>()
    var animations = HashMap<Int, Animation>()
    val frames: HashMultimap<Int, FrameDefinition> = HashMultimap.create()

    init {
        val library = CacheLibrary(CACHE_PATH)
        osrs = library.isOSRS
        println("OSRS cache: $osrs")
        loadEntities(library)
        println("Loaded ${entities.size} entities")
        loadItems(library)
        println("Loaded ${items.size} items")
        loadSequences(library)
        println("Loaded ${animations.size} sequences")
        loadFrames(library)
        println("Loaded ${frames.size()} frames")
        library.close()
    }

    private fun loadEntities(library: CacheLibrary) {
        if (!osrs) {
            val npcIdx = library.getIndex(IndexType.CONFIG.getIndexId(osrs)).getArchive(IndexType.NPC.getIndexId(osrs)).getFile("npc.idx")
            val npcArchive = library.getIndex(IndexType.CONFIG.getIndexId(osrs)).getArchive(IndexType.NPC.getIndexId(osrs)).getFile("npc.dat")

            val idxStream = InputStream317(npcIdx.data)
            val total = idxStream.readUShort()

            val streamIndices = IntArray(total)
            var offset = 2
            for (i in 0 until total) {
                streamIndices[i] = offset
                offset += idxStream.readUShort()
            }

            val stream = InputStream317(npcArchive.data)
            for (i in 0 until total) {
                stream.currentPosition = streamIndices[i]
                val npc = Loader317().loadEntity(i, stream)// NpcLoader().load(i, stream.readBytes())
                if (npc.models != null && npc.name.toLowerCase() != "null") {
                    entities[npc.id] = npc
                }
            }
            addPlayer()
            return
        }

        val npcLoader = NpcLoader()
        for (i in 0..library.getIndex(IndexType.CONFIG.getIndexId(osrs)).getArchive(IndexType.NPC.getIndexId(osrs)).lastFile.id) {
            val file = library.getIndex(IndexType.CONFIG.getIndexId(osrs)).getArchive(IndexType.NPC.getIndexId(osrs)).getFile(i)?: continue
            val npc = npcLoader.load(file.id, file.data)
            if (npc.models != null && npc.name.toLowerCase() != "null") {
                entities[npc.id] = npc
            }
        }
        addPlayer()
    }

    private fun addPlayer() {
        val player = NpcDefinition(0)
        player.name = "Player"
        player.models = intArrayOf(230, 249, 292, 151, 176, 254, 181)
        entities[player.id] = player
    }


    private fun loadItems(library: CacheLibrary) {
        if (!osrs) {
            val itemIdx = library.getIndex(IndexType.CONFIG.getIndexId(osrs)).getArchive(IndexType.ITEM.getIndexId(osrs)).getFile("obj.idx")
            val itemArchive = library.getIndex(IndexType.CONFIG.getIndexId(osrs)).getArchive(IndexType.ITEM.getIndexId(osrs)).getFile("obj.dat")

            val idxStream = InputStream317(itemIdx.data)
            val total = idxStream.readUShort()

            val streamIndices = IntArray(total)
            var offset = 2
            for (i in 0 until total) {
                streamIndices[i] = offset
                offset += idxStream.readUShort()
            }

            val stream = InputStream317(itemArchive.data)
            for (i in 0 until total) {
                stream.currentPosition = streamIndices[i]
                val item = Loader317().loadItem(i, stream)
                if (item.maleModel0 > 0 && item.name.toLowerCase() != "null") {
                    items[item.id] = item
                }
            }
            return
        }

        val itemLoader = ItemLoader()
        for (i in 0..library.getIndex(IndexType.CONFIG.getIndexId(osrs)).getArchive(IndexType.ITEM.getIndexId(osrs)).lastFile.id) {
            val file = library.getIndex(IndexType.CONFIG.getIndexId(osrs)).getArchive(IndexType.ITEM.getIndexId(osrs)).getFile(i)?: continue
            val item = itemLoader.load(file.id, file.data)
            if (item.maleModel0 > 0 && item.name.toLowerCase() != "null") {
                items[item.id] = item
            }
        }
    }

    private fun loadSequences(library: CacheLibrary) {

        if (!osrs) {
            val sequenceArchive = library.getIndex(IndexType.CONFIG.getIndexId(osrs))
                                         .getArchive(IndexType.SEQUENCE.getIndexId(osrs))
                                         .getFile("seq.dat")
            animations = Loader317().loadSequences(context, sequenceArchive.data)
            return
        }
        val sequenceLoader = SequenceLoader()
        for (i in 0..library.getIndex(IndexType.CONFIG.getIndexId(osrs)).getArchive(IndexType.SEQUENCE.getIndexId(osrs)).lastFile.id) {
            val file = library.getIndex(IndexType.CONFIG.getIndexId(osrs)).getArchive(IndexType.SEQUENCE.getIndexId(osrs)).getFile(i) ?: continue
            val sequence = sequenceLoader.load(file.id, file.data)
            val animation = Animation(context, sequence)
            animations[file.id] = animation
        }
    }

    private fun loadFrames(library: CacheLibrary) {
        val frameIndex = IndexType.FRAME.getIndexId(osrs) // TODO

        /*val lib = CacheLibrary(CACHE_317_PATH)
        for (i in 0..lib.getIndex(frameIndex).lastArchive.id) {
            println("LOADING FILE $i ---------")
            val file = lib.getIndex(frameIndex).getArchive(i).getFile(0)
            //Loader317().loadFrames(file.id, file.data, this)
        }
        lib.close()*/

        /*val frameLoader = FrameLoader()
        val frameMapLoader = FramemapLoader()

        val frameMapIndex = IndexType.FRAME_MAP.getIndexId(osrs)

        for (i in 0..library.getIndex(frameIndex).lastArchive.id) {
            val archive = library.getIndex(frameIndex).getArchive(i)?: continue

            for (j in 0..library.getIndex(frameIndex).getArchive(i).lastFile.id) {
                val frameFile = library.getIndex(frameIndex).getArchive(i).getFile(j)?: continue
                val frameData = frameFile.data

                val frameMapArchiveId = (frameData[0].toInt() and 0xff) shl 8 or (frameData[1].toInt() and 0xff)
                val frameMapFile = library.getIndex(frameMapIndex).getArchive(frameMapArchiveId).getFile(0)

                val frameMap = frameMapLoader.load(frameMapArchiveId, frameMapFile.data)
                val frame = frameLoader.load(frameMap, frameFile.id, frameData)
                frames.put(archive.id, frame)
            }
        }*/
    }

    fun pack(animation: Animation) {
        println("Packing ${animation.sequence.id}")
        val library = CacheLibrary(CACHE_PATH)
        val frameIndex = IndexType.FRAME.getIndexId(osrs)

        val maxArchiveId = frames.keySet().max()!!
        val newArchiveId = maxArchiveId + 1

        library.getIndex(frameIndex).addArchive(newArchiveId)

        for (keyframe in animation.keyframes) {
            val bytes = keyframe.encode()
            println("Packed frame ${keyframe.id} to archive $newArchiveId")
            library.getIndex(frameIndex).getArchive(newArchiveId).addFile(keyframe.id, bytes)
        }
        library.getIndex(frameIndex).update()

        val sequence = animation.toSequence()
        val data = animation.encode(sequence)

        library.getIndex(IndexType.CONFIG.getIndexId(osrs)).getArchive(IndexType.SEQUENCE.getIndexId(osrs)).addFile(sequence.id, data)
        library.getIndex(IndexType.CONFIG.getIndexId(osrs)).update()
        println("Packed ${animation.sequence.id}")
        library.close()
    }
}