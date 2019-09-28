import api.IAnimation
import api.ICachePacker
import api.ProgressListenerWrapper
import net.runelite.cache.definitions.FramemapDefinition
import net.runelite.cache.definitions.SequenceDefinition
import org.displee.CacheLibrary
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

class CachePackerLegacy317: ICachePacker {

    override fun toString() = "Legacy 317"

    override fun packAnimation(animation: IAnimation, archiveId: Int, library: CacheLibrary,
                               listener: ProgressListenerWrapper, maxAnimationId: Int) {

        val file = encodeAnimation(animation)
        library.getIndex(FRAME_INDEX).addArchive(archiveId)
        library.getIndex(FRAME_INDEX).getArchive(archiveId).addFile(0, file)
        library.getIndex(FRAME_INDEX).update(listener)
        packSequence(animation, archiveId, library, listener, maxAnimationId)
        library.close()
    }

    private fun encodeAnimation(animation: IAnimation): ByteArray {
        val out = ByteArrayOutputStream()
        val os = DataOutputStream(out)

        val frameMap = animation.getKeyframes().first().getFrameMapDef()
        os.write(encodeFrameMap(frameMap))

        val modified = animation.getKeyframes().filter { it.isModified() }
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
            for (j in def.frameMaps[i]) {
                os.writeShort(j)
            }
        }

        os.close()
        return out.toByteArray()
    }

    private fun packSequence(animation: IAnimation, archiveId: Int, library: CacheLibrary,
                             listener: ProgressListenerWrapper, maxAnimationId: Int) {
        listener.change(0.0, "Packing sequence definition...")
        val existingData = library.getIndex(CONFIG_INDEX).getArchive(SEQUENCE_INDEX).getFile("seq.dat").data

        // Change length
        val length = maxAnimationId + 1
        existingData[0] = ((length ushr 8) and 0xFF).toByte()
        existingData[1] = ((length ushr 0) and 0xFF).toByte()

        val sequence = animation.toSequence(archiveId)
        val newData = encodeSequence(sequence)

        // Combine data
        library.getIndex(CONFIG_INDEX).getArchive(SEQUENCE_INDEX).addFile("seq.dat", existingData + newData)
        library.getIndex(CONFIG_INDEX).update(listener)
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