package animation

import Processor
import net.runelite.cache.definitions.FrameDefinition
import net.runelite.cache.definitions.SequenceDefinition
import org.joml.Vector3i
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.min

class Animation(private val context: Processor, val sequence: SequenceDefinition) {

    // Copy constructor
    constructor(newId: Int, animation: Animation): this(animation.context, SequenceDefinition(newId)) {
        animation.keyframes.forEach {
            keyframes.add(Keyframe(it.id, it))
        }
        loaded = true
        modified = true
        maximumLength = getMaxLength()
    }

    var modified = false
    private var loaded = false
    val keyframes = ArrayList<Keyframe>()
    var maximumLength = 0

    fun load() {
        if (loaded) {
            return
        }

        for ((index, frameId) in sequence.frameIDs.withIndex()) {
            val frames = context.animationHandler.frames.get(frameId.ushr(16))
            val frameFileId = frameId and 0xFFFF
            val frame = frames.stream().filter { frame -> frame.id == frameFileId }.findFirst().get()

            val keyframe = Keyframe(index, sequence.frameLenghts[index])
            val frameMap = frame.framemap
            val references = ArrayDeque<Reference>()
            val maxId = frame.indexFrameIds.max() ?: continue

            for (id in 0..maxId) {
                val typeId = frameMap.types[id]
                if (typeId > TransformationType.SCALE.id) { // Alpha transformations unsupported
                    continue
                }

                val type = TransformationType.fromId(typeId)
                val transformation = Transformation(id, type, frameMap.frameMaps[id], getOffset(frame, id, type))

                if (transformation.type == TransformationType.REFERENCE) {
                    references.add(Reference(transformation))
                } else {
                    references.peekLast().group[transformation.type] = transformation
                }
            }

            var newId = 0
            for (reference in references) {
                reference.group.forEach { keyframe.add(it.value, newId++) }
            }
            keyframes.add(keyframe)
        }

        maximumLength = getMaxLength()
        loaded = true
    }

    private fun getOffset(frame: FrameDefinition, id: Int, type: TransformationType): Vector3i {
        val index = frame.indexFrameIds.indexOf(id)
        return when {
                index != -1 -> Vector3i(frame.translator_x[index], frame.translator_y[index], frame.translator_z[index])
                else -> type.getDefaultOffset()
        }
    }

    fun getMaxLength(): Int {
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
            val frameIndex = context.animationHandler.getFrameIndex(this)
            keyframes.remove(keyframes[frameIndex])
            updateKeyframes()
        }
    }

    private fun insertKeyframe(index: Int, keyframe: Keyframe) {
        keyframes.add(index, keyframe)
        context.animationHandler.setFrame(index, 0)
        updateKeyframes()
    }

    private fun updateKeyframes() {
        context.animationHandler.playPause(false)
        maximumLength = getMaxLength()
        context.gui.animationPanel.setTimeline()
    }

    fun modifyKeyframeLength(newLength: Int) {
        val index = context.animationHandler.getFrameIndex(this)
        val keyframe = keyframes[index]
        keyframe.length = newLength
        maximumLength = getMaxLength()
    }
}