package transfer

import render.RenderContext
import animation.*
import api.animation.TransformationType
import api.cache.stream.InputStream
import api.definition.FrameMapDefinition
import api.definition.SequenceDefinition
import org.joml.Vector3i
import util.FileDialog
import java.io.File

class ImportManager(private val context: RenderContext) {

    fun import() {
        val name = FileDialog.openFile(listOf("*.pgl")) ?: return
        val data = File(name).readBytes()
        importPgl(data)
    }

    private fun importPgl(data: ByteArray) {
        val animation = decodePgl(data)
        animation.length = animation.calculateLength()
        context.animationHandler.addAnimation(animation)
        context.animationHandler.load(animation)
    }

    private fun decodePgl(data: ByteArray): Animation {
        val stream = InputStream(data)
        val newId = context.cacheService.animations.keys.maxOrNull()!! + 1
        val sequence = SequenceDefinition(newId)

        val animation = Animation(context, sequence)
        animation.modified = true

        stream.readUnsignedByte() // Revision byte
        val n = stream.readUnsignedShort()

        repeat(n) {
            val length = stream.readUnsignedShort()
            val frameMap = FrameMapDefinition()
            frameMap.decode(stream)

            val keyframe = Keyframe(it, -1, length, frameMap)
            keyframe.decode(stream)
            keyframe.modified = true
            animation.keyframes.add(keyframe)
        }

        animation.sequence.leftHandItem = stream.readInt()
        animation.sequence.rightHandItem = stream.readInt()
        return animation
    }

    private fun FrameMapDefinition.decode(stream: InputStream) {
        length = stream.readUnsignedByte()
        types = IntArray(length)
        maps = Array(length) { IntArray(0) }

        repeat(length) {
            types[it] = stream.readUnsignedByte()
        }

        repeat(length) {
            maps[it] = IntArray(stream.readUnsignedByte())
        }

        repeat(length) {
            repeat (maps[it].size) { index ->
                maps[it][index] = stream.readUnsignedByte()
            }
        }
    }

    private fun Keyframe.decode(stream: InputStream) {
        frameMap.id = stream.readShort() // TODO: different between revisions
        val n = stream.readUnsignedByte()

        repeat(n) {
            val id = stream.readShort()
            val x = stream.readShort()
            val y = stream.readShort()
            val z = stream.readShort()

            val tf = Transformation(id, TransformationType.REFERENCE, frameMap.maps[id], Vector3i(x, y, z))
            val reference = ReferenceNode(tf)
            transformations.add(reference)

            val children = stream.readUnsignedByte()
            for (i in 0 until children) {
                val childId = stream.readShort()
                val childType = stream.readUnsignedByte()
                val childX = stream.readShort()
                val childY = stream.readShort()
                val childZ = stream.readShort()

                val type = TransformationType.fromId(childType)?: continue
                val child = Transformation(childId, type, frameMap.maps[childId], Vector3i(childX, childY, childZ))
                reference.children[type] = child
                transformations.add(child)
            }
        }
    }
}