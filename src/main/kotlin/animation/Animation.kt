package animation

import Processor
import gui.component.Popup
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
        length = calculateLength()
    }

    var modified = false
    var saved = false
    val keyframes = ArrayList<Keyframe>()
    var length = 0

    fun load() {
        if (keyframes.isNotEmpty()) { // Already loaded
            return
        }

        for ((index, frameId) in sequence.frameIDs.withIndex()) {
            val archiveId = frameId ushr 16
            val frameArchive = context.cacheService.getFrameArchive(archiveId)
            val frameFileId = frameId and 0xFFFF

            val frame = frameArchive.stream().filter { f -> f.id == frameFileId }.findFirst().get()
            val frameMap = frame.framemap
            val keyframe = Keyframe(index, frameId, sequence.frameLenghts[index], frameMap)

            val references = ArrayDeque<Reference>()
            val maxId = frame.indexFrameIds.max()?: continue

            for (id in 0..maxId) {
                val typeId = frameMap.types[id]
                if (typeId > TransformationType.SCALE.id) { // Alpha transformations unsupported
                    continue
                }

                val type = TransformationType.fromId(typeId)
                val transformation = Transformation(id, type, frameMap.frameMaps[id], getDelta(frame, id, type))

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

    private fun getDelta(frame: FrameDefinition, id: Int, type: TransformationType): Vector3i {
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
        } else {
            Popup("Invalid Operation", "Unable to delete the last keyframe", 260f, 70f).show(context.frame)
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

        var modified = 0 // To decrement keyframe id's if necessary
        for (i in 0 until keyframes.size) {
            val keyframe = keyframes[i]
            sequence.frameLenghts[i] = keyframe.length

            sequence.frameIDs[i] = if (keyframe.modified) {
                ((archiveId and 0xFFFF) shl 16) or (modified++ and 0xFFFF) // New frame id
            } else {
                keyframe.frameId // Original frame id
            }
        }
        return sequence
    }
}