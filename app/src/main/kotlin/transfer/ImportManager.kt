package transfer

import Processor
import animation.*
import net.runelite.cache.definitions.FramemapDefinition
import net.runelite.cache.definitions.SequenceDefinition
import net.runelite.cache.io.InputStream
import org.joml.Vector3i
import util.FileDialogs
import java.io.File

class ImportManager(private val context: Processor) {

    fun import() {
        val name = FileDialogs.openFile(listOf("*.pgl"), ".")?: return
        val data = File(name).readBytes()
        importPgl(data)
    }

    private fun importPgl(data: ByteArray) {
        val animation = decodePgl(data)
        animation.length = animation.calculateLength()

        context.cacheService.animations[animation.sequence.id] = animation
        context.gui.listPanel.animationList.addElement(animation)
        context.animationHandler.load(animation)
    }

    private fun decodePgl(data: ByteArray): Animation {
        val stream = InputStream(data)
        val newId = context.cacheService.animations.keys.max()!! + 1
        val sequence = SequenceDefinition(newId)

        val animation = Animation(context, sequence)
        animation.modified = true

        val revision = stream.readUnsignedByte()
        val n = stream.readUnsignedShort()

        for (i in 0 until n) {
            val length = stream.readUnsignedShort()

            val frameMap = FramemapDefinition()
            frameMap.decode(stream)

            val keyframe = Keyframe(i, -1, length, frameMap)
            keyframe.decode(stream)

            keyframe.modified = true
            animation.keyframes.add(keyframe)
        }

        animation.sequence.leftHandItem = stream.readUnsignedShort()
        animation.sequence.rightHandItem = stream.readUnsignedShort()
        return animation
    }

    private fun FramemapDefinition.decode(stream: InputStream) {
        id = -1
        length = stream.readUnsignedByte()
        types = IntArray(length)
        frameMaps = arrayOfNulls<IntArray>(length)

        for (i in 0 until length) {
            types[i] = stream.readUnsignedByte()
        }

        for (i in 0 until length) {
            frameMaps[i] = IntArray(stream.readUnsignedByte())
        }

        for (i in 0 until length) {
            for (j in 0 until frameMaps[i].size) {
                frameMaps[i][j] = stream.readUnsignedByte()
            }
        }
    }

    private fun Keyframe.decode(stream: InputStream) {
        frameMap.id = stream.readShort().toInt()
        val n = stream.readUnsignedByte()

        for (i in 0 until n) {
            val id = stream.readShort().toInt()
            val x = stream.readShort().toInt()
            val y = stream.readShort().toInt()
            val z = stream.readShort().toInt()

            val tf = Transformation(id, TransformationType.REFERENCE, frameMap.frameMaps[id], Vector3i(x, y, z))
            val reference = ReferenceNode(tf)
            transformations.add(reference)

            val children = stream.readUnsignedByte()
            for (j in 0 until children) {
                val childId = stream.readShort().toInt()
                val childType = stream.readUnsignedByte()
                val childX = stream.readShort().toInt()
                val childY = stream.readShort().toInt()
                val childZ = stream.readShort().toInt()

                val type = TransformationType.fromId(childType)
                val child = Transformation(childId, type, frameMap.frameMaps[childId], Vector3i(childX, childY, childZ))
                reference.children[type] = child
                transformations.add(child)
            }
        }
    }
}