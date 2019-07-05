package cache.pack

import CACHE_PATH
import animation.Animation
import cache.CacheService
import cache.IndexType
import cache.ProgressListener
import org.displee.CacheLibrary

class CachePacker317(private val service: CacheService): CachePacker {

    override fun packAnimation(animation: Animation, listener: ProgressListener) {
        val library = CacheLibrary(CACHE_PATH)
        val file = animation.getFile317()?: return

        val frameIndex = IndexType.FRAME.getIndexId(false)
        val newArchiveId = service.getMaxFrameArchive(library) + 1

        library.getIndex(frameIndex).addArchive(newArchiveId)
        library.getIndex(frameIndex).getArchive(newArchiveId).addFile(0, file)
        library.getIndex(frameIndex).update(listener)

        packSequence(animation, newArchiveId, listener, library)
        library.close()
    }

    private fun packSequence(animation: Animation, archiveId: Int, listener: ProgressListener, library: CacheLibrary) {
        listener.change(0.0, "Packing sequence definition...")
        val existingData = library.getIndex(IndexType.CONFIG.getIndexId(false))
            .getArchive(IndexType.SEQUENCE.getIndexId(false))
            .getFile("seq.dat").data

        val length = service.animations.size
        // Change length
        existingData[0] = ((length ushr 8) and 0xFF).toByte()
        existingData[1] = ((length ushr 0) and 0xFF).toByte()

        val sequence = animation.toSequence(archiveId)
        val newData = animation.encodeSequence317(sequence)

        // Combine data
        library.getIndex(IndexType.CONFIG.getIndexId(false))
            .getArchive(IndexType.SEQUENCE.getIndexId(false))
            .addFile("seq.dat", existingData + newData)

        library.getIndex(IndexType.CONFIG.getIndexId(false)).update(listener)
    }
}