package api.animation

import org.joml.Vector3i

enum class TransformationType(val id: Int) {

    REFERENCE(0),
    TRANSLATION(1),
    ROTATION(2),
    SCALE(3);
    // Alpha transformations (5) currently unsupported

    companion object {
        private val map = values().associateBy { it.id }
        fun fromId(type: Int) = map[type]
    }

    fun getDefaultOffset(): Vector3i {
        val offset = if (this == SCALE) 128 else 0
        return Vector3i(offset)
    }

    override fun toString(): String {
        return super.toString().toLowerCase().capitalize()
    }
}