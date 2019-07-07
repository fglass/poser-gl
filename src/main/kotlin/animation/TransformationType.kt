package animation

import org.joml.Vector3i

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