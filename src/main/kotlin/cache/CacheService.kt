package cache

import CACHE_PATH
import Processor
import animation.Animation
import com.google.common.collect.HashMultimap
import entity.EntityComponent
import net.runelite.cache.definitions.FrameDefinition
import net.runelite.cache.definitions.ItemDefinition
import net.runelite.cache.definitions.ModelDefinition
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
        addPlayer()
        loadNpcDefinitions(library)
        println("Loaded ${entities.size} entities")
        loadItemDefinitions(library)
        println("Loaded ${items.size} items")
        loadSequences(library)
        println("Loaded ${animations.size} sequences")
        library.close()
    }

    private fun loadNpcDefinitions(library: CacheLibrary) {
        if (!osrs) {
            val npcIdx = library.getIndex(IndexType.CONFIG.getIndexId(osrs))
                .getArchive(IndexType.NPC.getIndexId(osrs))
                .getFile("npc.idx")
            val npcArchive = library.getIndex(IndexType.CONFIG.getIndexId(osrs))
                .getArchive(IndexType.NPC.getIndexId(osrs))
                .getFile("npc.dat")

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
                val npc = Loader317().loadEntityDefinition(i, stream)
                if (npc.models != null && npc.name.toLowerCase() != "null" && npc.name != "") {
                    entities[npc.id] = npc
                }
            }
            return
        }

        val npcLoader = NpcLoader()
        val maxIndex = library.getIndex(IndexType.CONFIG.getIndexId(osrs))
            .getArchive(IndexType.NPC.getIndexId(osrs))
            .lastFile.id

        for (i in 0..maxIndex) {
            val file = library.getIndex(IndexType.CONFIG.getIndexId(osrs))
                .getArchive(IndexType.NPC.getIndexId(osrs))
                .getFile(i)?: continue

            val npc = npcLoader.load(file.id, file.data)
            if (npc.models != null && npc.name.toLowerCase() != "null" && npc.name != "") {
                entities[npc.id] = npc
            }
        }
    }

    private fun addPlayer() {
        val player = NpcDefinition(-1)
        player.name = "Player"
        player.models = intArrayOf(230, 249, 292, 151, 176, 254, 181)
        entities[player.id] = player
    }

    private fun loadItemDefinitions(library: CacheLibrary) {
        if (!osrs) {
            val itemIdx = library.getIndex(IndexType.CONFIG.getIndexId(osrs))
                .getArchive(IndexType.ITEM.getIndexId(osrs))
                .getFile("obj.idx")
            val itemArchive = library.getIndex(IndexType.CONFIG.getIndexId(osrs))
                .getArchive(IndexType.ITEM.getIndexId(osrs))
                .getFile("obj.dat")

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
                val item = Loader317().loadItemDefinition(i, stream)
                if (item.maleModel0 > 0 && item.name.toLowerCase() != "null") {
                    items[item.id] = item
                }
            }
            return
        }

        val itemLoader = ItemLoader()
        val maxIndex = library.getIndex(IndexType.CONFIG.getIndexId(osrs))
            .getArchive(IndexType.ITEM.getIndexId(osrs))
            .lastFile.id

        for (i in 0..maxIndex) {
            val file = library.getIndex(IndexType.CONFIG.getIndexId(osrs))
                .getArchive(IndexType.ITEM.getIndexId(osrs))
                .getFile(i)?: continue

            val item = itemLoader.load(file.id, file.data)
            if (item.maleModel0 > 0 && item.name.toLowerCase() != "null") {
                items[item.id] = item
            }
        }
    }

    fun loadModelDefinition(component: EntityComponent): ModelDefinition {
        val library = CacheLibrary(CACHE_PATH)
        val model = library.getIndex(IndexType.MODEL.getIndexId(library.isOSRS)).getArchive(component.id).getFile(0)
        val def = ModelLoader().load(component.id, model.data)

        if (component.originalColours != null && component.newColours != null) {
            for (i in 0 until component.originalColours.size) {
                def.recolor(component.originalColours[i], component.newColours[i])
            }
        }
        library.close()
        return def
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
        val maxIndex = library.getIndex(IndexType.CONFIG.getIndexId(osrs))
            .getArchive(IndexType.SEQUENCE.getIndexId(osrs))
            .lastFile.id

        for (i in 0..maxIndex) {
            val file = library.getIndex(IndexType.CONFIG.getIndexId(osrs))
                .getArchive(IndexType.SEQUENCE.getIndexId(osrs))
                .getFile(i) ?: continue

            val sequence = sequenceLoader.load(file.id, file.data)
            val animation = Animation(context, sequence)
            animations[file.id] = animation
        }
    }

    fun getFrameArchive(archiveId: Int): MutableSet<FrameDefinition> {
        if (archiveId !in frames.keySet()) {
            loadFrameArchive(archiveId)
        }
        return frames.get(archiveId)
    }

    private fun loadFrameArchive(archiveId: Int) {
        val library = CacheLibrary(CACHE_PATH)
        val frameIndex = IndexType.FRAME.getIndexId(osrs)

        if (!osrs) {
            val loader = Loader317()
            val file = library.getIndex(frameIndex).getArchive(archiveId).getFile(0)

            if (file.data.isNotEmpty()) {
                loader.loadFrameFile(archiveId, file.data, this)
            }
            return
        }

        val archive = library.getIndex(frameIndex).getArchive(archiveId)?: return
        val frameLoader = FrameLoader()
        val frameMapLoader = FramemapLoader()
        val frameMapIndex = IndexType.FRAME_MAP.getIndexId(osrs)

        for (j in 0..library.getIndex(frameIndex).getArchive(archiveId).lastFile.id) {
            val frameFile = library.getIndex(frameIndex).getArchive(archiveId).getFile(j)?: continue
            val frameData = frameFile.data

            val frameMapArchiveId = (frameData[0].toInt() and 0xff) shl 8 or (frameData[1].toInt() and 0xff)
            val frameMapFile = library.getIndex(frameMapIndex).getArchive(frameMapArchiveId).getFile(0)

            val frameMap = frameMapLoader.load(frameMapArchiveId, frameMapFile.data)
            val frame = frameLoader.load(frameMap, frameFile.id, frameData)
            frames.put(archive.id, frame)
        }
        library.close()
    }

    fun pack(animation: Animation) {
        if (animation.modified) {
            if (osrs) packAnimation(animation) else packAnimation317(animation)
        }
    }

    private fun packAnimation(animation: Animation) {
        println("Packing sequence ${animation.sequence.id}")
        val library = CacheLibrary(CACHE_PATH)
        val frameIndex = IndexType.FRAME.getIndexId(osrs)

        val maxArchiveId = getMaxFrameArchive(library)
        val newArchiveId = maxArchiveId + 1

        library.getIndex(frameIndex).addArchive(newArchiveId)

        animation.keyframes.forEach {
            if (it.modified) {
                library.getIndex(frameIndex).getArchive(newArchiveId).addFile(it.id, it.encode(osrs))
            }
        }
        library.getIndex(frameIndex).update()
        println("Packed frames to archive $newArchiveId")

        packSequence(animation, newArchiveId, library)
        library.close()
    }

    private fun packSequence(animation: Animation, archiveId: Int, library: CacheLibrary) {
        val sequence = animation.toSequence(archiveId)
        val data = animation.encodeSequence(sequence)

        library.getIndex(IndexType.CONFIG.getIndexId(osrs))
            .getArchive(IndexType.SEQUENCE.getIndexId(osrs))
            .addFile(sequence.id, data)

        library.getIndex(IndexType.CONFIG.getIndexId(osrs)).update()
        println("Packed sequence definition ${animation.sequence.id}")
    }

    private fun packAnimation317(animation: Animation) {
        println("Packing sequence ${animation.sequence.id}")
        val library = CacheLibrary(CACHE_PATH)

        val frameIndex = IndexType.FRAME.getIndexId(osrs)
        val newArchiveId = getMaxFrameArchive(library) + 1
        library.getIndex(frameIndex).addArchive(newArchiveId)

        val frames = animation.getFile317()?: return
        library.getIndex(frameIndex).getArchive(newArchiveId).addFile(0, frames)
        library.getIndex(frameIndex).update()
        println("Packed frames to archive $newArchiveId")

        packSequence317(animation, newArchiveId, library)
        library.close()
    }

    private fun packSequence317(animation: Animation, archiveId: Int, library: CacheLibrary) {
        val existingData = library.getIndex(IndexType.CONFIG.getIndexId(osrs))
            .getArchive(IndexType.SEQUENCE.getIndexId(osrs))
            .getFile("seq.dat").data

        val length = animations.size

        // Adjust length
        existingData[0] = ((length ushr 8) and 0xFF).toByte()
        existingData[1] = ((length ushr 0) and 0xFF).toByte()

        val sequence = animation.toSequence(archiveId)
        val newData = animation.encodeSequence317(sequence)

        // Combine data
        library.getIndex(IndexType.CONFIG.getIndexId(osrs))
            .getArchive(IndexType.SEQUENCE.getIndexId(osrs))
            .addFile("seq.dat", existingData + newData)

        library.getIndex(IndexType.CONFIG.getIndexId(osrs)).update()
        println("Packed sequence definition ${animation.sequence.id}")
    }

    private fun getMaxFrameArchive(library: CacheLibrary): Int {
        val frameIndex = IndexType.FRAME.getIndexId(osrs)
        return library.getIndex(frameIndex).lastArchive.id
    }
}