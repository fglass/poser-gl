package gizmo

import org.joml.Matrix4f
import org.joml.Rayf
import org.joml.Vector3f
import render.RenderContext

/**
 * https://nelari.us/post/gizmos/
 */
abstract class Gizmo {

    var active = false
    var position = Vector3f(0f, 0f, 0f)
    internal var selectedAxis: GizmoAxis? = null

    abstract fun render(context: RenderContext, viewMatrix: Matrix4f, ray: Rayf)

    abstract fun deactivate()
}