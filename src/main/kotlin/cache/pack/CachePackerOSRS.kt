package cache.pack

import CACHE_PATH
import animation.Animation
import cache.CacheService
import cache.IndexType
import cache.ProgressListener
import org.displee.CacheLibrary

class CachePackerOSRS(private val service: CacheService): CachePacker {

    override fun packAnimation(animation: Animation, listener: ProgressListener) {
        val library = CacheLibrary(CACHE_PATH)
        val frameIndex = IndexType.FRAME.getIndexId(true)
        val newArchiveId = service.getMaxFrameArchive(library) + 1
        library.getIndex(frameIndex).addArchive(newArchiveId)

        var modified = 0 // To decrement keyframe id's if necessary
        animation.keyframes.forEach {
            if (it.modified) {
                library.getIndex(frameIndex).getArchive(newArchiveId).addFile(modified++, it.encode())
            }
        }
        library.getIndex(frameIndex).update(listener)

        packSequence(animation, newArchiveId, listener, library)
        library.close()
    }

    private fun packSequence(animation: Animation, archiveId: Int, listener: ProgressListener, library: CacheLibrary) {
        listener.change(0.0, "Packing sequence definition...")
        val sequence = animation.toSequence(archiveId)
        val data = animation.encodeSequence(sequence)

        library.getIndex(IndexType.CONFIG.getIndexId(true))
            .getArchive(IndexType.SEQUENCE.getIndexId(true))
            .addFile(sequence.id, data)

        library.getIndex(IndexType.CONFIG.getIndexId(true)).update(listener)
    }
}