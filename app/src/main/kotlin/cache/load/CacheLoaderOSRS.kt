package cache.load

import render.RenderContext
import animation.Animation
import cache.IndexType
import net.runelite.cache.definitions.FrameDefinition
import net.runelite.cache.definitions.ItemDefinition
import net.runelite.cache.definitions.NpcDefinition
import net.runelite.cache.definitions.loaders.*
import org.displee.CacheLibrary
import java.util.HashSet

class CacheLoaderOSRS(private val context: RenderContext): ICacheLoader {

    override fun loadSequences(library: CacheLibrary): HashMap<Int, Animation> {
        val animations = HashMap<Int, Animation>()
        val sequenceLoader = SequenceLoader()
        val maxIndex = library.getIndex(IndexType.CONFIG.idOsrs)
            .getArchive(IndexType.SEQUENCE.idOsrs)
            .lastFile.id

        for (i in 0..maxIndex) {
            val file = library.getIndex(IndexType.CONFIG.idOsrs)
                .getArchive(IndexType.SEQUENCE.idOsrs)
                .getFile(i) ?: continue

            val sequence = sequenceLoader.load(file.id, file.data)
            val animation = Animation(context, sequence)
            animations[file.id] = animation
        }
        return animations
    }

    override fun loadFrameArchive(archiveId: Int, library: CacheLibrary): HashSet<FrameDefinition> {
        val frames = HashSet<FrameDefinition>()
        val frameIndex = IndexType.FRAME.idOsrs
        val frameLoader = FrameLoader()
        val frameMapLoader = FramemapLoader()
        val frameMapIndex = IndexType.FRAME_MAP.idOsrs

        for (j in 0..library.getIndex(frameIndex).getArchive(archiveId).lastFile.id) {
            val frameFile = library.getIndex(frameIndex).getArchive(archiveId).getFile(j)?: continue
            val frameData = frameFile.data

            val frameMapArchiveId = (frameData[0].toInt() and 0xff) shl 8 or (frameData[1].toInt() and 0xff)
            val frameMapFile = library.getIndex(frameMapIndex).getArchive(frameMapArchiveId).getFile(0)

            val frameMap = frameMapLoader.load(frameMapArchiveId, frameMapFile.data)
            val frame = frameLoader.load(frameMap, frameFile.id, frameData)
            frames.add(frame)
        }
        return frames
    }

    override fun loadNpcDefintions(library: CacheLibrary): HashMap<Int, NpcDefinition> {
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