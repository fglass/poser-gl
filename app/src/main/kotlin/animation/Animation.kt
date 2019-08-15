package animation

import render.RenderContext
import gui.component.Dialog
import mu.KotlinLogging
import net.runelite.cache.definitions.*
import org.joml.Vector3f
import org.joml.Vector3i
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet
import kotlin.collections.LinkedHashMap
import kotlin.math.min

private val logger = KotlinLogging.logger {}

const val ITEM_OFFSET = 512

class Animation(private val context: RenderContext, val sequence: SequenceDefinition) {

    // Copy constructor
    constructor(newId: Int, animation: Animation): this(animation.context, SequenceDefinition(newId)) {
        animation.keyframes.forEach {
            keyframes.add(Keyframe(it.id, it))
        }
        sequence.leftHandItem = animation.sequence.leftHandItem
        sequence.rightHandItem = animation.sequence.rightHandItem
        length = calculateLength()
        modified = true
    }

    var modified = false
    val keyframes = ArrayList<Keyframe>()
    var length = 0

    fun load() {
        if (keyframes.isEmpty()) { // Animation already loaded
            parseSequence()
            length = calculateLength()
        }
        setRootNode()
    }

    private fun parseSequence() {
        val frames = LinkedHashMap<Int, FrameDefinition>() // Preserve insertion order
        val indices = HashSet<Int>() // Accumulate frame indices

        for ((index, frameId) in sequence.frameIDs.withIndex()) {
            val archiveId = frameId ushr 16
            val frameArchive = context.cacheService.frames.get(archiveId)
            val frameFileId = frameId and 0xFFFF

            val frame: FrameDefinition
            try {
                frame = frameArchive.first { f -> f.id == frameFileId }
            } catch (e: NoSuchElementException) {
                logger.info { "Failed to load frame $frameFileId in archive $archiveId" }
                continue
            }

            frames[index] = frame // Keep index in case frame fails to load above
            frame.indexFrameIds.forEach {
                indices.add(it)
            }
        }

        for (frame in frames) {
            val frameMap = frame.value.framemap
            val keyframe = Keyframe(frame.key, sequence.frameIDs[frame.key], sequence.frameLenghts[frame.key], frameMap)
            val references = ArrayDeque<ReferenceNode>()

            for (id in indices) {
                val typeId = frameMap.types[id]
                if (typeId > TransformationType.SCALE.id) { // Alpha transformations unsupported
                    continue
                }

                val type = TransformationType.fromId(typeId)
                val transformation = Transformation(id, type, frameMap.frameMaps[id], getDelta(frame.value, id, type))

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
            for (other in references) {
                if (other.id == reference.id) {
                    continue
                }
                reference.trySetParent(other)
            }
        }
    }

    private fun setRootNode() { // TODO: shifting on modification
        var root: ReferenceNode? = null
        for (transformation in keyframes.first().transformations) {
            if (transformation is ReferenceNode) {
                val rotation = transformation.getRotation()?: continue
                if (root == null || rotation.frameMap.size > root.getRotation()!!.frameMap.size) {
                    root = transformation
                }
            }
        }
        context.nodeRenderer.rootNode = root
    }

    fun calculateLength(): Int {
        return min(keyframes.sumBy { it.length }, MAX_LENGTH)
    }

    fun equipItems() {
        equipItem(sequence.leftHandItem)
        equipItem(sequence.rightHandItem)
    }

    private fun equipItem(id: Int) {
        if (id >= ITEM_OFFSET) {
            val item = context.cacheService.items[id - ITEM_OFFSET]?: return
            context.entity?.addItem(item, context.entityHandler)
        }
    }

    fun findSiblings(): IntArray {
        load()
        val matching = context.cacheService.frameMaps[getFrameMap().id]?: return emptyArray<Int>().toIntArray()
        val siblings = matching.toIntArray()
        siblings.sort()
        return siblings
    }

    fun getFrameMap(): FramemapDefinition {
        return keyframes.first().frameMap
    }

    fun addKeyframe() {
        val newIndex = context.animationHandler.getFrameIndex(this) + 1
        val keyframe = Keyframe(keyframes.size, keyframes[newIndex - 1]) // Copy previous
        insertKeyframe(newIndex, keyframe)
    }

    fun copyKeyframe() {
        val index = context.animationHandler.getFrameIndex(this)
        val keyframe = keyframes[index]
        context.animationHandler.copiedFrame = keyframe
        Dialog("Keyframe Action", "Successfully copied keyframe ${keyframe.id}", context, 260f, 70f).display()
    }

    fun pasteKeyframe() {
        val copied = context.animationHandler.copiedFrame
        if (copied.id != -1) {
            val keyframe = Keyframe(keyframes.size, copied) // Copy after to avoid shared references
            val newIndex = context.animationHandler.getFrameIndex(this) + 1
            insertKeyframe(newIndex, keyframe)
        }
    }

    fun interpolateKeyframes() {
        if (keyframes.size < 2) {
            Dialog("Invalid Operation", "Insufficient number of keyframes", context, 260f, 70f).display()
            return
        }

        val index = context.animationHandler.getFrameIndex(this)
        val first = keyframes[index]
        val second = keyframes[index + 1]

        val largest = if (first.transformations.size > second.transformations.size) first else second
        val smallest = if (largest == first) second else first
        val interpolated = Keyframe(keyframes.size, largest)

        repeat(smallest.transformations.size) {
            val delta = Vector3f(first.transformations[it].delta).lerp(Vector3f(second.transformations[it].delta), 0.5f)
            interpolated.transformations[it].delta = Vector3i(delta.x.toInt(), delta.y.toInt(), delta.z.toInt())
        }
        insertKeyframe(index + 1, interpolated)
    }

    fun deleteKeyframe() {
        if (keyframes.size > 1) {
            val index = context.animationHandler.getFrameIndex(this)
            keyframes.remove(keyframes[index])
            updateKeyframes()
        } else {
            Dialog("Invalid Operation", "Unable to delete the last keyframe", context, 260f, 70f).display()
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

        sequence.leftHandItem = this.sequence.leftHandItem
        sequence.rightHandItem = this.sequence.rightHandItem

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