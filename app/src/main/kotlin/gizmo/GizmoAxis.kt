package gizmo

import org.joml.Vector3f
import org.joml.Vector4f

class GizmoAxis(val type: AxisType, val colour: Vector4f, val rotation: Vector3f) {
    var previousIntersection = Vector3f(0f)
}

enum class AxisType {  // Can rely on ordinal in this context
    X,
    Y,
    Z
}