package animation

import api.animation.IAnimation
import api.animation.TransformationType
import api.definition.FrameDefinition
import api.definition.FrameMapDefinition
import api.definition.SequenceDefinition
import render.RenderContext
import mu.KotlinLogging
import org.joml.Vector3i
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap
import kotlin.math.min

private val logger = KotlinLogging.logger {}
const val ITEM_OFFSET = 512

class Animation(private val context: RenderContext, var sequence: SequenceDefinition): IAnimation {

    // Copy constructor
    constructor(newId: Int, animation: Animation): this(animation.context, SequenceDefinition(newId)) {
        animation.keyframes.forEach {
            keyframes.add(Keyframe(it.id, it))
        }

        sequence.frameIds = animation.sequence.frameIds
        sequence.frameLengths = animation.sequence.frameLengths
        sequence.leftHandItem = animation.sequence.leftHandItem
        sequence.rightHandItem = animation.sequence.rightHandItem
        sequence.loopOffset = animation.sequence.loopOffset

        length = calculateLength()
        modified = true
    }

    override val keyframes = ArrayList<Keyframe>()
    var modified = false
    var length = 0

    fun load() {
        if (keyframes.isEmpty()) {
            parseSequence()
            length = calculateLength()
        }
    }

    fun reload() {
        keyframes.clear()
        load()
    }

    private fun parseSequence() {
        val frames = LinkedHashMap<Int, FrameDefinition>() // Preserve insertion order
        val transformationIndices = TreeSet<Int>() // Sorted by values

        // Pre-process sequence frames
        for ((index, frameId) in sequence.frameIds.withIndex()) {
            val archiveId = frameId ushr 16
            val frameArchive = context.cacheService.getFrameArchive(archiveId)
            val frameFileId = frameId and 0xFFFF

            val frame: FrameDefinition
            try {
                frame = frameArchive.first { f -> f.id == frameFileId }
            } catch (e: NoSuchElementException) {
                logger.warn("Failed to load frame $frameFileId in archive $archiveId")
                continue
            }

            val indices = if (context.settingsManager.advancedMode) {
                val maxId = frame.indices.maxOrNull() ?: continue
                0..maxId // Use entire range
            } else {
                frame.indices.toSet()
            }

            transformationIndices.addAll(indices)
            frames[index] = frame // Preserve index in case frame fails to load
        }

        for ((frameIndex, frameDefinition) in frames) {
            val frameMap = frameDefinition.frameMap
            val keyframe = Keyframe(
                frameIndex, sequence.frameIds[frameIndex], sequence.frameLengths[frameIndex], frameMap
            )

            for (index in transformationIndices) {
                val type = TransformationType.fromId(frameDefinition.frameMap.types[index]) ?: continue

                if (type != TransformationType.REFERENCE) {
                    continue
                }

                val reference = ReferenceNode(
                    Transformation(index, type, frameMap.maps[index], getDelta(frameDefinition, index, type))
                )

                reference.findChildren(index, frameDefinition)

                if (reference.children.size > 0) { // Ignore lone references
                    keyframe.transformations.add(reference)
                    reference.children.forEach {
                        keyframe.transformations.add(it.value)
                    }
                }
            }

            keyframe.buildSkeleton()
            keyframes.add(keyframe)
        }
    }

    private fun getDelta(frame: FrameDefinition, id: Int, type: TransformationType): Vector3i {
        val index = frame.indices.indexOf(id)
        return when {
                index != -1 -> Vector3i(frame.deltaX[index], frame.deltaY[index], frame.deltaZ[index])
                else -> type.getDefaultOffset()
        }
    }

    private fun ReferenceNode.findChildren(id: Int, frame: FrameDefinition) { // Allows additional children to be found
        val frameMap = frame.frameMap
        var childId = id + 1

        if (childId >= frameMap.types.size) {
            return
        }

        var childType = frameMap.types[childId]

        // Search transformations until encounter next reference
        while (childType != TransformationType.REFERENCE.id) {
            TransformationType.fromId(childType)?.let {
                val child = Transformation(childId, it, frameMap.maps[childId], getDelta(frame, childId, it))
                children[it] = child
            }

            // Check next transformation
            if (++childId < frameMap.types.size) {
                childType = frameMap.types[childId]
            } else {
                break
            }
        }
    }

    fun calculateLength(): Int {
        return min(keyframes.sumBy { it.length }, MAX_LENGTH)
    }

    fun toggleItems(equip: Boolean) {
        toggleItem(sequence.leftHandItem, equip)
        toggleItem(sequence.rightHandItem, equip)
    }

    private fun toggleItem(id: Int, equip: Boolean) {
        if (id < ITEM_OFFSET) {
            return
        }

        val item = context.cacheService.items[id - ITEM_OFFSET] ?: return
        context.entityHandler.entity?.let {
            val action = if (equip) it::addItem else it::removeItem
            action.invoke(item, context.entityHandler)
        }
    }

    fun getFrameMap(): FrameMapDefinition {
        return keyframes.first().frameMap
    }

    fun getFrameIndex(index: Int) = Math.floorMod(index, keyframes.size)

    fun insertKeyframe(keyframe: Keyframe, index: Int) {
        keyframes.add(index, keyframe)
        context.animationHandler.setCurrentFrame(index)
        updateKeyframes()
    }

    fun removeKeyframe(keyframe: Keyframe) {
        keyframes.remove(keyframe)
        updateKeyframes()
    }

    fun removeKeyframeAt(index: Int) {
        keyframes.removeAt(index)
        updateKeyframes()
    }

    private fun updateKeyframes() {
        context.animationHandler.setPlay(false)
        length = calculateLength()
        context.gui.animationPanel.setTimeline()
    }

    override fun toSequence(archiveId: Int): SequenceDefinition { // TODO: move?
        val sequence = SequenceDefinition(sequence.id)
        sequence.leftHandItem = this.sequence.leftHandItem
        sequence.rightHandItem = this.sequence.rightHandItem
        sequence.loopOffset = this.sequence.loopOffset

        sequence.frameLengths = IntArray(keyframes.size)
        sequence.frameIds = IntArray(keyframes.size)

        var modified = 0 // To decrement keyframe id's if necessary
        for (i in 0 until keyframes.size) {
            val keyframe = keyframes[i]
            sequence.frameLengths[i] = keyframe.length

            sequence.frameIds[i] = if (keyframe.modified) {
                ((archiveId and 0xFFFF) shl 16) or (modified++ and 0xFFFF) // New frame id
            } else {
                keyframe.frameId // Original frame id
            }
        }
        return sequence
    }
}