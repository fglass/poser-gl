package cache.pack

import animation.Animation
import cache.CacheService
import cache.IndexType
import cache.ProgressListener
import cache.load.CacheLoader317
import net.runelite.cache.definitions.FramemapDefinition
import net.runelite.cache.definitions.SequenceDefinition
import org.displee.CacheLibrary
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

class CachePacker317(private val service: CacheService): CachePacker {

    override fun packAnimation(animation: Animation, listener: ProgressListener) {
        val library = CacheLibrary(service.cachePath)
        val file = encodeAnimation(animation)

        val frameIndex = IndexType.FRAME.id317
        val newArchiveId = service.getMaxFrameArchive(library) + 1

        library.getIndex(frameIndex).addArchive(newArchiveId)
        library.getIndex(frameIndex).getArchive(newArchiveId).addFile(0, file)
        library.getIndex(frameIndex).update(listener)

        packSequence(animation, newArchiveId, listener, library)
        library.close()
    }

    fun encodeAnimation(animation: Animation): ByteArray {
        val out = ByteArrayOutputStream()
        val os = DataOutputStream(out)

        val frameMap = animation.keyframes.first().frameMap
        os.write(encodeFrameMap(frameMap))

        val modified = animation.keyframes.filter { it.modified }
        os.writeShort(modified.size)

        for ((i, keyframe) in modified.withIndex()) { // To decrement keyframe id
            os.write(keyframe.encode(i, false))
        }

        os.close()
        return out.toByteArray()
    }

    private fun encodeFrameMap(def: FramemapDefinition): ByteArray {
        val out = ByteArrayOutputStream()
        val os = DataOutputStream(out)

        os.writeShort(def.length)

        for (i in 0 until def.length) {
            os.writeShort(def.types[i])
        }

        for (i in 0 until def.length) {
            os.writeShort(def.frameMaps[i].size)
        }

        for (i in 0 until def.length) {
            for (j in 0 until def.frameMaps[i].size) {
                os.writeShort(def.frameMaps[i][j])
            }
        }

        os.close()
        return out.toByteArray()
    }

    private fun packSequence(animation: Animation, archiveId: Int, listener: ProgressListener, library: CacheLibrary) {
        listener.change(0.0, "Packing sequence definition...")
        val existingData = library.getIndex(IndexType.CONFIG.id317)
            .getArchive(IndexType.SEQUENCE.id317)
            .getFile("seq.dat").data

        val length = service.animations.keys.max()!! + 1
        // Change length
        existingData[0] = ((length ushr 8) and 0xFF).toByte()
        existingData[1] = ((length ushr 0) and 0xFF).toByte()

        val sequence = animation.toSequence(archiveId)
        val newData = if (service.loader is CacheLoader317) {
            CachePackerOSRS(service).encodeSequence(sequence)
        } else {
            encodeSequence(sequence)
        }

        // Combine data
        library.getIndex(IndexType.CONFIG.id317)
            .getArchive(IndexType.SEQUENCE.id317)
            .addFile("seq.dat", existingData + newData)

        library.getIndex(IndexType.CONFIG.id317).update(listener)
    }

    private fun encodeSequence(sequence: SequenceDefinition): ByteArray {
        val out = ByteArrayOutputStream()
        val os = DataOutputStream(out)

        os.writeByte(1)
        os.writeShort(sequence.frameIDs.size)

        for (frameId in sequence.frameIDs) {
            os.writeInt(frameId)
        }

        for (length in sequence.frameLenghts) {
            os.writeByte(length)
        }

        if (sequence.leftHandItem != -1) {
            os.writeByte(6)
            os.writeShort(sequence.leftHandItem)
        }
        if (sequence.rightHandItem != -1) {
            os.writeByte(7)
            os.writeShort(sequence.rightHandItem)
        }

        os.writeByte(0)
        os.close()
        return out.toByteArray()
    }
}