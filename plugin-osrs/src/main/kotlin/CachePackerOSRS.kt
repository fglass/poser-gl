import api.IAnimation
import api.ICachePacker
import api.ProgressListenerWrapper
import net.runelite.cache.definitions.SequenceDefinition
import org.displee.CacheLibrary
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

class CachePackerOSRS: ICachePacker {

    override fun toString() = "OSRS"

    override fun packAnimation(animation: IAnimation, archiveId: Int, library: CacheLibrary,
                               listener: ProgressListenerWrapper, maxAnimationId: Int) {

        library.getIndex(FRAME_INDEX).addArchive(archiveId)
        var modified = 0 // To decrement keyframe id's if necessary
        animation.getKeyframes().forEach {
            if (it.isModified()) {
                library.getIndex(FRAME_INDEX).getArchive(archiveId).addFile(modified++, it.encode())
            }
        }
        library.getIndex(FRAME_INDEX).update(listener)

        packSequence(animation, archiveId, library, listener)
        library.close()
    }

    private fun packSequence(animation: IAnimation, archiveId: Int, library: CacheLibrary,
                             listener: ProgressListenerWrapper) {

        listener.change(0.0, "Packing sequence definition...")
        val sequence = animation.toSequence(archiveId)
        val data = encodeSequence(sequence)

        library.getIndex(CONFIG_INDEX).getArchive(SEQUENCE_INDEX).addFile(sequence.id, data)
        library.getIndex(CONFIG_INDEX).update(listener)
    }

    private fun encodeSequence(sequence: SequenceDefinition): ByteArray {
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