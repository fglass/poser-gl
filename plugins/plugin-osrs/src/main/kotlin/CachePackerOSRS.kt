import api.*
import net.runelite.cache.definitions.SequenceDefinition
import org.displee.CacheLibrary
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

class CachePackerOSRS: ICachePacker {

    override fun toString() = "OSRS"

    override fun packAnimation(animation: IAnimation, library: CacheLibrary,
                               listener: ProgressListenerWrapper, maxAnimationId: Int) {

        val archiveId = getMaxFrameArchive(library) + 1
        library.getIndex(FRAME_INDEX).addArchive(archiveId)

        var modified = 0 // To decrement keyframe id's if necessary
        animation.keyframes.forEach {
            if (it.modified) {
                library.getIndex(FRAME_INDEX).getArchive(archiveId).addFile(modified++, encodeKeyframe(it))
            }
        }
        library.getIndex(FRAME_INDEX).update(listener)

        packSequence(animation, archiveId, library, listener)
        library.close()
    }

    override fun getMaxFrameArchive(library: CacheLibrary) = library.getIndex(FRAME_INDEX).lastArchive.id

    private fun encodeKeyframe(keyframe: IKeyframe): ByteArray {
        val out = ByteArrayOutputStream()
        val os = DataOutputStream(out)

        os.writeShort(keyframe.frameMap.id)
        val length = keyframe.transformations.maxBy { it.id }?.id?: -1
        os.writeByte(length + 1)

        // Write masks first
        var index = 0
        for (transformation in keyframe.transformations) {
            // Insert ignored masks to preserve transformation indices
            repeat(transformation.id - index) {
                os.writeByte(0)
            }
            index = transformation.id + 1

            val mask = getMask(transformation.delta)
            os.writeByte(mask)
        }

        // Write transformation values
        for (transformation in keyframe.transformations) {
            val mask = getMask(transformation.delta)
            if (mask == 0) {
                continue
            }

            if (mask and 1 != 0) {
                writeSmartShort(os, transformation.delta.x)
            }

            if (mask and 2 != 0) {
                writeSmartShort(os, transformation.delta.y)
            }

            if (mask and 4 != 0) {
                writeSmartShort(os, transformation.delta.z)
            }
        }
        os.close()
        return out.toByteArray()
    }

    private fun writeSmartShort(os: DataOutputStream, value: Int) {
        if (value >= -64 && value < 64) {
            os.writeByte(value + 64)
        } else if (value >= -16384 && value < 16384) {
            os.writeShort(value + 49152)
        }
    }

    private fun packSequence(animation: IAnimation, archiveId: Int, library: CacheLibrary,
                             listener: ProgressListenerWrapper) {

        listener.change(0.0, "Packing sequence definition...")
        val sequence = animation.toSequence(archiveId)
        val data = encodeSequence(sequence)

        library.getIndex(CONFIG_INDEX).getArchive(SEQUENCE_INDEX).addFile(sequence.id, data)
        library.getIndex(CONFIG_INDEX).update(listener)
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
        if (sequence.leftHandItem != -1) {
            os.writeByte(6)
            os.writeShort(sequence.leftHandItem)
        }
        if (sequence.rightHandItem != -1) {
            os.writeByte(7)
            os.writeShort(sequence.rightHandItem)
        }

        os.writeByte(0) // Opcode 0: End of definition
        os.close()
        return out.toByteArray()
    }
}