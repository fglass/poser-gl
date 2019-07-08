package animation

import Processor
import gui.component.Dialog
import mu.KotlinLogging
import net.runelite.cache.definitions.FrameDefinition
import net.runelite.cache.definitions.SequenceDefinition
import org.joml.Vector3f
import org.joml.Vector3i
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.min

private val logger = KotlinLogging.logger {}

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

            val frame: FrameDefinition
            try {
                frame = frameArchive.stream().filter { f -> f.id == frameFileId }.findFirst().get()
            } catch (e: NoSuchElementException) {
                logger.info { "Failed to load frame $frameFileId in archive $archiveId" }
                continue
            }

            val frameMap = frame.framemap
            val keyframe = Keyframe(index, frameId, sequence.frameLenghts[index], frameMap)

            val references = ArrayDeque<ReferenceNode>()
            val maxId = frame.indexFrameIds.max()?: continue

            for (id in 0..maxId) {
                val typeId = frameMap.types[id]
                if (typeId > TransformationType.SCALE.id) { // Alpha transformations unsupported
                    continue
                }

                val type = TransformationType.fromId(typeId)
                val transformation = Transformation(id, type, frameMap.frameMaps[id], getDelta(frame, id, type))

                if (transformation.type == TransformationType.REFERENCE) {
                    references.add(ReferenceNode(transformation))
                } else if (references.size > 0) {
                    references.peekLast().children[transformation.type] = transformation
                }
            }

            for (reference in references) {
                keyframe.transformations.add(reference)
                reference.children.forEach { keyframe.transformations.add(it.value) }
            }
            keyframes.add(keyframe)
            constructSkeleton(references)
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

    private fun constructSkeleton(references: ArrayDeque<ReferenceNode>) {
        for (reference in references) {
            val frameMap = reference.frameMap.toSet()
            for (node in references) {
                if (reference == node) {
                    continue
                }

                val rotation = node.children[TransformationType.ROTATION]?: continue
                // Parent if frame map is superset
                if (rotation.frameMap.toSet().containsAll(frameMap)) {
                    reference.parent = node
                }
            }
        }
    }

    fun calculateLength(): Int {
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

    fun interpolateKeyframes() {
        val index = context.animationHandler.getFrameIndex(this)
        val first = keyframes[index]
        val second = keyframes[index + 1]

        val interpolated = Keyframe(keyframes.size, first) // TODO different size transformations

        for ((i, transformation) in interpolated.transformations.withIndex()) {
            val delta = Vector3f(first.transformations[i].delta).lerp(Vector3f(second.transformations[i].delta), 0.5f)
            transformation.delta = Vector3i(delta.x.toInt(), delta.y.toInt(), delta.z.toInt())
        }
        insertKeyframe(index + 1, interpolated)
    }

    fun deleteKeyframe() {
        if (keyframes.size > 1) {
            val index = context.animationHandler.getFrameIndex(this)
            keyframes.remove(keyframes[index])
            updateKeyframes()
        } else {
            Dialog("Invalid Operation", "Unable to delete the last keyframe", 260f, 70f).show(context.frame)
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