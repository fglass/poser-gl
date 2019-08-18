package gizmo

import entity.Camera
import org.joml.Vector3f
import render.RenderContext

/**
 * https://nelari.us/post/gizmos/
 */
abstract class Gizmo { // TODO: refactor

    var active = false
    var position = Vector3f(0f, 0f, 0f)
    internal var selectedAxis: GizmoAxis? = null

    abstract fun render(context: RenderContext, camera: Camera)

    abstract fun endTransform()
}