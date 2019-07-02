package animation

import Processor
import net.runelite.cache.definitions.FrameDefinition
import net.runelite.cache.definitions.SequenceDefinition
import net.runelite.cache.definitions.loaders.SequenceLoader
import org.joml.Vector3i
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.util.*
import kotlin.NoSuchElementException
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
        length = calculateLength()
    }

    private var loaded = false
    var modified = false
    val keyframes = ArrayList<Keyframe>()
    var length = 0

    fun load() {
        if (loaded) {
            return
        }

        for ((index, frameId) in sequence.frameIDs.withIndex()) {
            val frames = context.cacheService.frames.get(frameId ushr 16)
            val frameFileId = frameId and 0xFFFF

            val frame = frames.stream().filter { f -> f.id == frameFileId }.findFirst().get()
            val keyframe = Keyframe(index, frameId, sequence.frameLenghts[index])
            val frameMap = frame.framemap
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
        loaded = true
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
        keyframes[index].length = newLength
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

    fun toSequence(): SequenceDefinition {
        // If (!modified) TODO
        val sequence = SequenceDefinition(sequence.id)
        sequence.frameLenghts = IntArray(keyframes.size)
        sequence.frameIDs = IntArray(keyframes.size)

        //var maxOffset = 0

        val maxArchiveId = context.cacheService.frames.keySet().max()!!
        val newArchiveId = maxArchiveId + 1

        for (i in 0 until keyframes.size) {
            val keyframe = keyframes[i]
            sequence.frameLenghts[i] = keyframe.length

            val frameId = keyframe.frameId
            //val archiveId = frameId ushr 16

           /* val frames = context.animationHandler.frames.get(archiveId)
            val frameFileId = frameId and 0xFFFF
            val frame = frames.stream().filter { frame -> frame.id == frameFileId }.findFirst().get()*/

            //val maxFileId = frames.maxBy { it.id }!!.id
            //val newFileId = maxFileId + ++maxOffset

            // Use new archive file with reset file ids or same archive and maxFileId?
            // Solution: Put in new archive file, but any unmodified keyframe has an untouched frameId
            val newFrameId = ((newArchiveId and 0xFFFF) shl 16) or (i and 0xFFFF)

            //val id1 = newFrameId ushr 16
            //val id2 = newFrameId and 0xFFFF

            //println("Original frame $newFrameId archive $newArchiveId file $i new $id1 $id2")
            sequence.frameIDs[i] = if (keyframe.modified) newFrameId else frameId
        }
        return sequence
    }

    //val buf = encode(sequence)
    //val test2 = SequenceLoader().loadFrames(-1, buf)
    fun encode(sequence: SequenceDefinition): ByteArray {
        val out = ByteArrayOutputStream()
        val os = DataOutputStream(out)

        os.writeByte(1) // Opcode 1
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

        os.writeByte(0) // Opcode 0
        os.close()
        return out.toByteArray()
    }
}