package cache.pack

import animation.Animation
import cache.CacheService
import cache.IndexType
import cache.ProgressListener
import net.runelite.cache.definitions.SequenceDefinition
import org.displee.CacheLibrary
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

class CachePackerOSRS(private val service: CacheService): CachePacker {

    override fun packAnimation(animation: Animation, listener: ProgressListener) {
        val library = CacheLibrary(service.cachePath)
        val frameIndex = IndexType.FRAME.idOsrs
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
        val data = encodeSequence(sequence)

        library.getIndex(IndexType.CONFIG.idOsrs)
            .getArchive(IndexType.SEQUENCE.idOsrs)
            .addFile(sequence.id, data)

        library.getIndex(IndexType.CONFIG.idOsrs).update(listener)
    }

    fun encodeSequence(sequence: SequenceDefinition): ByteArray {
        val out = ByteArrayOutputStream()
        val os = DataOutputStream(out)

        os.writeByte(1) // Opcode 1: Starting frames
        os.writeShort(sequence.frameIDs.size)

        for (length in sequence.frameLenghts) {
            os.writeShort(length)
        }

        for (frameId in sequence.frameIDs) {
            os.writeShort(frameId and 0xFFFF)
        }

        for (frameId in sequence.frameIDs) {
            os.writeShort(frameId ushr 16)
        }

        // Other sequence attributes
        os.writeByte(6)
        os.writeShort(sequence.leftHandItem)
        os.writeByte(7)
        os.writeShort(sequence.rightHandItem)

        os.writeByte(0) // Opcode 0: End of definition
        os.close()
        return out.toByteArray()
    }
}