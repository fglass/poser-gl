package cache.load

import render.RenderContext
import animation.Animation
import cache.CacheService
import cache.IndexType
import net.runelite.cache.definitions.loaders.*
import org.displee.CacheLibrary

class CacheLoaderOSRS(private val context: RenderContext, private val service: CacheService):
      CacheLoader {

    override fun loadSequences(library: CacheLibrary) {
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
            service.animations[file.id] = animation
        }
    }

    override fun loadFrameArchive(archiveId: Int, library: CacheLibrary) {
        val frameIndex = IndexType.FRAME.idOsrs

        val archive = library.getIndex(frameIndex).getArchive(archiveId)?: return
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
            service.frames.put(archive.id, frame)
        }
    }

    override fun loadNpcDefintions(library: CacheLibrary) {
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
                service.entities[npc.id] = npc
            }
        }
    }

    override fun loadItemDefinitions(library: CacheLibrary) {
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
                service.items[item.id] = item
            }
        }
    }
}