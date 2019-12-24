package api.animation

import org.joml.Vector3i

interface ITransformation {
    val id: Int
    val type: TransformationType
    val delta: Vector3i
}

fun getMask(delta: Vector3i): Int {
    val x = if (delta.x != 0) 1 else 0
    val y = if (delta.y != 0) 2 else 0
    val z = if (delta.z != 0) 4 else 0
    return x or y or z
}