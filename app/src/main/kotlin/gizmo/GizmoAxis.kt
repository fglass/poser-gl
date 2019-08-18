package gizmo

import org.joml.Vector3f
import org.joml.Vector4f

class GizmoAxis(val type: AxisType, val colour: Vector4f, val rotation: Vector3f) {
    var previousIntersection = Vector3f(0f)
}

enum class AxisType(val index: Int) { // Use index to avoid relying on ordinal
    X(0),
    Y(1),
    Z(2)
}