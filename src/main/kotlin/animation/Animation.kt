package animation

import net.runelite.cache.definitions.FrameDefinition
import net.runelite.cache.definitions.SequenceDefinition
import org.joml.Vector3i
import com.google.common.collect.HashMultimap
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.min

class Animation(val sequence: SequenceDefinition, private val frames: HashMultimap<Int, FrameDefinition>) {

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
}