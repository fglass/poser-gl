package animation

import Processor
import net.runelite.cache.definitions.FrameDefinition
import net.runelite.cache.definitions.SequenceDefinition
import org.joml.Vector3i
import com.google.common.collect.HashMultimap
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.min

class Animation(private val context: Processor, val sequence: SequenceDefinition,
                private val frames: HashMultimap<Int, FrameDefinition>) {

    val keyframes = ArrayList<Keyframe>()
    var maximumLength: Int

    init {
        load()
        maximumLength = getMaxLength()
    }

    private fun load() {
        for ((index, frameId) in sequence.frameIDs.withIndex()) {
            val frames = frames.get(frameId.ushr(16))
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
        val keyframe = keyframes[newIndex - 1].copy(keyframes.size) // Copy previous
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
            val keyframe = copied.copy(keyframes.size) // Copy after to avoid shared references
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
        maximumLength = getMaxLength()
        context.gui.animationPanel.setTimeline()
    }
}