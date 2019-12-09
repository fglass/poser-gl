package animation

import api.IAnimation
import render.RenderContext
import mu.KotlinLogging
import net.runelite.cache.definitions.*
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
        sequence.frameIDs = animation.sequence.frameIDs
        sequence.leftHandItem = animation.sequence.leftHandItem
        sequence.rightHandItem = animation.sequence.rightHandItem
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
        val references = TreeSet<Int>() // Sorted by values

        // Pre-process sequence frames
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

            val indices = if (context.settingsManager.advancedMode) {
                val max = frame.indexFrameIds.max() ?: return
                (0..max).toList()
            } else {
                frame.indexFrameIds.filter { frame.framemap.types[it] == TransformationType.REFERENCE.id }
            }
            references.addAll(indices) // Accumulate reference indices across animation
            frames[index] = frame // Preserve index in case frame fails to load
        }

        for (frame in frames) {
            val frameMap = frame.value.framemap
            val keyframe = Keyframe(frame.key, sequence.frameIDs[frame.key], sequence.frameLenghts[frame.key], frameMap)

            for (id in references) {
                val type = TransformationType.REFERENCE
                val transformation = Transformation(id, type, frameMap.frameMaps[id], getDelta(frame.value, id, type))
                val reference = ReferenceNode(transformation)
                reference.findChildren(id, frame.value)

                if (reference.children.size > 0) { // Ignore lone references
                    keyframe.transformations.add(reference)
                    reference.children.forEach {
                        keyframe.transformations.add(it.value)
                    }
                }
            }

            constructSkeleton(keyframe.transformations)
            keyframes.add(keyframe)
        }
    }

    private fun getDelta(frame: FrameDefinition, id: Int, type: TransformationType): Vector3i {
        val index = frame.indexFrameIds.indexOf(id)
        return when {
                index != -1 -> Vector3i(frame.translator_x[index], frame.translator_y[index], frame.translator_z[index])
                else -> type.getDefaultOffset()
        }
    }

    private fun ReferenceNode.findChildren(id: Int, frame: FrameDefinition) { // Allows additional children to be found
        val frameMap = frame.framemap
        var childId = id + 1
        if (childId >= frameMap.types.size) {
            return
        }
        var childType = frameMap.types[childId]

        // Search transformations until encounter next reference
        while (childType != TransformationType.REFERENCE.id) {
            TransformationType.fromId(childType)?.let {
                val child = Transformation(childId, it, frameMap.frameMaps[childId], getDelta(frame, childId, it))
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

        val item = context.cacheService.items[id - ITEM_OFFSET]?: return
        context.entity?.let {
            val action = if (equip) it::addItem else it::removeItem
            action.invoke(item, context.entityHandler)
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

    fun insertKeyframe(keyframe: Keyframe, index: Int) {
        keyframes.add(index, keyframe)
        context.animationHandler.setCurrentFrame(index, 0)
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

    override fun toSequence(archiveId: Int): SequenceDefinition {
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