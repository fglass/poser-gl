package gizmo

import org.joml.Vector3f
import org.joml.Vector4f

class GizmoAxis(val type: AxisType, val colour: Vector4f, var rotation: Vector3f) {

    var previousIntersection = Vector3f()
    private val defaultRotation = Vector3f(rotation)

    fun reset() {
        rotation = Vector3f(defaultRotation)
    }
}

enum class AxisType {  // Can rely on ordinal in this context
    X,
    Y,
    Z
}