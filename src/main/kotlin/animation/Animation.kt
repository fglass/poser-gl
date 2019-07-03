package animation

import Processor
import net.runelite.cache.definitions.FrameDefinition
import net.runelite.cache.definitions.FramemapDefinition
import net.runelite.cache.definitions.SequenceDefinition
import org.joml.Vector3i
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.min

class Animation(private val context: Processor, val sequence: SequenceDefinition) {

    // Copy constructor
    constructor(newId: Int, animation: Animation): this(animation.context, SequenceDefinition(newId)) {
        animation.keyframes.forEach {
            keyframes.add(Keyframe(it.id, it))
        }
        modified = true
        frameMap = animation.frameMap
        length = calculateLength()
    }

    var modified = false
    var frameMap: FramemapDefinition? = null
    val keyframes = ArrayList<Keyframe>()
    var length = 0

    fun load() {
        if (keyframes.isNotEmpty()) { // Already loaded
            return
        }

        for ((index, frameId) in sequence.frameIDs.withIndex()) {
            val frameArchive = context.cacheService.getFrameArchive(frameId ushr 16)
            val frameFileId = frameId and 0xFFFF

            val frame = frameArchive.stream().filter { f -> f.id == frameFileId }.findFirst().get()
            val keyframe = Keyframe(index, frameId, sequence.frameLenghts[index])

            val frameMap = frame.framemap
            this.frameMap = frameMap // Same for each frame

            val references = ArrayDeque<Reference>()
            val maxId = frame.indexFrameIds.max()?: continue

            for (id in 0..maxId) {
                val typeId = frameMap.types[id]
                if (typeId > TransformationType.SCALE.id) { // Alpha transformations unsupported
                    continue
                }

                val type = TransformationType.fromId(typeId)
                val transformation = Transformation(id, type, frameMap.id, frameMap.frameMaps[id],
                                                    getOffset(frame, id, type))

                if (transformation.type == TransformationType.REFERENCE) {
                    references.add(Reference(transformation))
                } else if (references.size > 0) {
                    references.peekLast().children[transformation.type] = transformation
                }
            }

            var newId = 0
            for (reference in references) {
                keyframe.add(reference, newId++)
                reference.children.forEach { keyframe.add(it.value, newId++) }
            }
            keyframes.add(keyframe)
        }
        length = calculateLength()
    }

    private fun getOffset(frame: FrameDefinition, id: Int, type: TransformationType): Vector3i {
        val index = frame.indexFrameIds.indexOf(id)
        return when {
                index != -1 -> Vector3i(frame.translator_x[index], frame.translator_y[index], frame.translator_z[index])
                else -> type.getDefaultOffset()
        }
    }

    private fun calculateLength(): Int {
        return min(keyframes.sumBy { it.length }, MAX_LENGTH)
    }

    fun addKeyframe() {
        val newIndex = context.animationHandler.getFrameIndex(this) + 1
        val keyframe = Keyframe(keyframes.size, keyframes[newIndex - 1]) // Copy previous
        insertKeyframe(newIndex, keyframe)
    }

    fun copyKeyframe() {
        val index = context.animationHandler.getFrameIndex(this)
        context.animationHandler.copiedFrame = keyframes[index]
    }

    fun pasteKeyframe() {
        val copied = context.animationHandler.copiedFrame
        if (copied.id != -1) {
            val newIndex = context.animationHandler.getFrameIndex(this) + 1
            val keyframe = Keyframe(keyframes.size, copied) // Copy after to avoid shared references
            insertKeyframe(newIndex, keyframe)
        }
    }

    fun deleteKeyframe() {
        if (keyframes.size > 1) {
            val index = context.animationHandler.getFrameIndex(this)
            keyframes.remove(keyframes[index])
            updateKeyframes()
        }
    }

    fun changeKeyframeLength(newLength: Int) {
        val index = context.animationHandler.getFrameIndex(this)
        val keyframe = keyframes[index]
        keyframe.length = newLength
        keyframe.modified = true
        length = calculateLength()

        context.animationHandler.setFrame(context.animationHandler.frameCount, 0) // Restart frame
        context.gui.animationPanel.setTimeline()
    }

    private fun insertKeyframe(index: Int, keyframe: Keyframe) {
        keyframes.add(index, keyframe)
        context.animationHandler.setFrame(index, 0)
        updateKeyframes()
    }

    private fun updateKeyframes() {
        context.animationHandler.setPlay(false)
        length = calculateLength()
        context.gui.animationPanel.setTimeline()
    }

    fun toSequence(archiveId: Int): SequenceDefinition {
        val sequence = SequenceDefinition(sequence.id)
        sequence.frameLenghts = IntArray(keyframes.size)
        sequence.frameIDs = IntArray(keyframes.size)

        for (i in 0 until keyframes.size) {
            val keyframe = keyframes[i]
            sequence.frameLenghts[i] = keyframe.length

            val newFrameId = ((archiveId and 0xFFFF) shl 16) or (i and 0xFFFF)
            sequence.frameIDs[i] = if (keyframe.modified) newFrameId else keyframe.frameId
        }
        return sequence
    }

    fun encodeSequence(sequence: SequenceDefinition): ByteArray {
        val out = ByteArrayOutputStream()
        val os = DataOutputStream(out)

        os.writeByte(1) // Opcode 1: Starting frames
        os.writeShort(keyframes.size)

        for (keyframe in keyframes) {
            os.writeShort(keyframe.length)
        }

        for (frameId in sequence.frameIDs) {
            os.writeShort(frameId and 0xFFFF)
        }

        for (frameId in sequence.frameIDs) {
            os.writeShort(frameId ushr 16)
        }

        os.writeByte(0) // Opcode 0: End of definition
        os.close()
        return out.toByteArray()
    }

    fun encodeSequence317(sequence: SequenceDefinition): ByteArray {
        val out = ByteArrayOutputStream()
        val os = DataOutputStream(out)

        os.writeByte(1)
        os.writeShort(keyframes.size)

        for (frameId in sequence.frameIDs) {
            os.writeInt(frameId)
        }

        for (keyframe in keyframes) {
            os.writeByte(keyframe.length)
        }

        os.writeByte(0)
        os.close()
        return out.toByteArray()
    }

    fun getFile317(): ByteArray? {
        val out = ByteArrayOutputStream()
        val os = DataOutputStream(out)

        val frameMap = this.frameMap?: return null // TODO issues with copy/pasting in 317
        os.write(encodeFrameMap317(frameMap))

        os.writeShort(keyframes.size)
        keyframes.forEach {
            if (it.modified) {
                os.write(it.encode(false))
            }
        }

        os.close()
        return out.toByteArray()
    }

    private fun encodeFrameMap317(def: FramemapDefinition): ByteArray {
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
}