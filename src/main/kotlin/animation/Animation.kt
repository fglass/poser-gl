package animation

import net.runelite.cache.definitions.ModelDefinition
import net.runelite.cache.definitions.SequenceDefinition
import org.joml.Vector3i
import java.util.*
import kotlin.collections.ArrayList

class Animation(private val sequence: SequenceDefinition, private val handler: AnimationHandler) {

    val keyframes = ArrayList<Keyframe>()

    init {
        load()
    }

    private fun load() {
        for ((index, frameId) in sequence.frameIDs.withIndex()) {
            val frames = handler.frames.get(frameId.ushr(16))
            val frameFileId = frameId and 0xFFFF
            val frame = frames.stream().filter { frame -> frame.id == frameFileId }.findFirst().get()

            val keyframe = Keyframe(sequence.frameLenghts[index])
            val frameMap = frame.framemap
            val references = ArrayDeque<Reference>()

            for (i in 0 until frame.translatorCount) {
                val id = frame.indexFrameIds[i]
                val type = frameMap.types[id]

                if (type > TransformationType.SCALE.id) { // Alpha transformations unsupported
                    continue
                }

                val transformation = Transformation(
                    id, TransformationType.fromInt(type), frameMap.frameMaps[id],
                    Vector3i(frame.translator_x[i], frame.translator_y[i], frame.translator_z[i])
                )

                if (transformation.type == TransformationType.REFERENCE) {
                    references.add(Reference(transformation))
                } else {
                    references.peekLast().children[transformation.type.id - 1] = transformation
                }
            }

            var newId = 0 // TODO: Clean-up
            for (reference in references) {
                reference.id = newId++
                keyframe.transformations.add(reference)

                for (link in reference.children) {
                    link.id = newId++
                    keyframe.transformations.add(link)
                }
            }

            keyframes.add(keyframe)
        }
    }
}

class Keyframe(val length: Int) {
    val transformations = ArrayList<Transformation>()
}

open class Transformation(var id: Int, val type: TransformationType, val frameMap: IntArray, val offset: Vector3i) {

    fun apply(def: ModelDefinition) {
        def.animate(type.id, frameMap, offset.x, offset.y, offset.z)
    }
}

class Reference(transformation: Transformation):
      Transformation(transformation.id, transformation.type, transformation.frameMap, transformation.offset) {

    val children = ArrayList<Transformation>()

    init {
        children.add(Transformation(id + 1, TransformationType.TRANSLATION, frameMap, Vector3i(0, 0, 0)))
        children.add(Transformation(id + 2, TransformationType.ROTATION, frameMap, Vector3i(0, 0, 0)))
        children.add(Transformation(id + 3, TransformationType.SCALE, frameMap, Vector3i(128, 128, 128)))
    }
}

enum class TransformationType(val id: Int) {

    REFERENCE(0),
    TRANSLATION(1),
    ROTATION(2),
    SCALE(3);

    companion object {
        private val map = values().associateBy { it.id }
        fun fromInt(type: Int) = map[type]?: throw IllegalArgumentException()
    }

    override fun toString(): String {
        return super.toString().toLowerCase().capitalize()
    }
}