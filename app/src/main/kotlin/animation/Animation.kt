package animation

import api.IAnimation
import api.IKeyframe
import render.RenderContext
import gui.component.Dialog
import mu.KotlinLogging
import net.runelite.cache.definitions.*
import org.joml.Vector3f
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

    var modified = false
    override val keyframes = ArrayList<Keyframe>()
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

            val indices = frame.indexFrameIds.filter { frame.framemap.types[it] == TransformationType.REFERENCE.id }
            references.addAll(indices) // Accumulate reference indices across animation
            frames[index] = frame // Keep index in case frame fails to load
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


        /*println("animation ${sequence.id}") //TODO: remove debug
        for (keyframe in keyframes) {
            println("--- keyframe ${keyframe.id} len ${keyframe.length} fm ${keyframe.frameMap.id} frame ${keyframe.frameId}")
            for (transformation in keyframe.transformations) {
                println("transformation ${transformation.id} type ${transformation.type} xyz ${transformation.delta.x} ${transformation.delta.y} ${transformation.delta.z}")
            }
        }*/
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

    private fun setRootNode() {
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

    fun addKeyframe() {
        val newIndex = context.animationHandler.getFrameIndex(this) + 1
        val keyframe = Keyframe(keyframes.size, keyframes[newIndex - 1]) // Copy previous
        insertKeyframe(newIndex, keyframe)
    }

    fun copyKeyframe() {
        val index = context.animationHandler.getFrameIndex(this)
        val keyframe = keyframes[index]
        context.animationHandler.copiedFrame = keyframe
        Dialog("Keyframe Action", "Successfully copied keyframe ${keyframe.id}", context, 200f, 70f).display()
    }

    fun pasteKeyframe() {
        val copied = context.animationHandler.copiedFrame
        if (copied.frameMap.id != getFrameMap().id) { // TODO: prevent animation copy
            Dialog("Invalid Operation", "Skeletons do not match", context, 200f, 70f).display()
            return
        }

        if (copied.id != -1) {
            val keyframe = Keyframe(keyframes.size, copied) // Copy after to avoid shared references
            val newIndex = context.animationHandler.getFrameIndex(this) + 1
            insertKeyframe(newIndex, keyframe)
        }
    }

    fun interpolateKeyframes() { // TODO: prevent animation copy
        if (keyframes.size < 2) {
            Dialog("Invalid Operation", "Insufficient number of keyframes", context, 200f, 70f).display()
            return
        }

        val index = context.animationHandler.getFrameIndex(this)
        if (index >= keyframes.size - 1) {
            Dialog("Invalid Operation", "No subsequent keyframe to interpolate with", context, 250f, 70f).display()
            return
        }

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
            Dialog("Invalid Operation", "Unable to delete the last keyframe", context, 200f, 70f).display()
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