package animation

import net.runelite.cache.definitions.ModelDefinition
import org.joml.Vector3i
import java.util.LinkedHashMap

open class Transformation(var id: Int, val type: TransformationType, var frameMap: IntArray, var offset: Vector3i) {

    constructor(transformation: Transformation): this(transformation.id, transformation.type,
                                                      transformation.frameMap, Vector3i(transformation.offset))

    fun apply(def: ModelDefinition) {
        def.animate(type.id, frameMap, offset.x, offset.y, offset.z)
    }
}

class Reference(transformation: Transformation): Transformation(transformation) {

    var group = LinkedHashMap<TransformationType, Transformation>() // TODO children

    init {
        group[type] = this
    }

    fun hasType(type: TransformationType): Boolean {
        return group.containsKey(type)
    }
}

enum class TransformationType(val id: Int) {

    REFERENCE(0),
    TRANSLATION(1),
    ROTATION(2),
    SCALE(3);

    companion object {
        private val map = values().associateBy { it.id }
        fun fromId(type: Int) = map[type]?: throw IllegalArgumentException()
    }

    fun getDefaultOffset(): Vector3i {
        return if (this == SCALE) Vector3i(128, 128, 128) else Vector3i(0, 0, 0)
    }

    override fun toString(): String {
        return super.toString().toLowerCase().capitalize()
    }
}