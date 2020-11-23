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
        if (keyframes.isEmpty()) { // Animation already loaded
            parseSequence()
            length = calculateLength()
        }
        setRootNode()
    }

    fun reload() {
        keyframes.clear()
        load()
    }

    private fun parseSequence() {
        val frames = LinkedHashMap<Int, FrameDefinition>() // Preserve insertion order
        val indices = TreeSet<Int>() // Sorted by values

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

            val frameIndices = if (context.settingsManager.advancedMode) {
                // Use all possible indices
                val maxId = frame.indices.maxOrNull() ?: continue
                (0..maxId).toList()
            } else {
                // Accumulate reference indices across animation
                frame.indices.filter { frame.frameMap.types[it] == TransformationType.REFERENCE.id }
            }

            indices.addAll(frameIndices)
            frames[index] = frame // Preserve index in case frame fails to load
        }

        for (frame in frames) {
            val frameMap = frame.value.frameMap
            val keyframe = Keyframe(frame.key, sequence.frameIds[frame.key], sequence.frameLengths[frame.key], frameMap)

            for (index in indices) {
                val type = TransformationType.fromId(frame.value.frameMap.types[index]) ?: continue
                val transformation = Transformation(
                    index, type, frameMap.maps[index], getDelta(frame.value, index, type)
                )

                if (type == TransformationType.REFERENCE) {
                    val reference = ReferenceNode(transformation)
                    reference.findChildren(index, frame.value)

                    if (reference.children.size > 0) { // Ignore lone references
                        keyframe.transformations.add(reference)
                        reference.children.forEach {
                            keyframe.transformations.add(it.value)
                        }
                    }
                }
            }

            constructSkeleton(keyframe.transformations)
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

    private fun constructSkeleton(transformations: ArrayList<Transformation>) { // TODO: tree structure
        val references = transformations.filterIsInstance<ReferenceNode>()
        for (reference in references) {
            for (other in references) {
                reference.trySetParent(other)
            }
        }
    }

    fun setRootNode() {
        var root: ReferenceNode? = null
        for (transformation in keyframes.first().transformations) {
            if (transformation is ReferenceNode) {
                val rotation = transformation.getRotation() ?: continue
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