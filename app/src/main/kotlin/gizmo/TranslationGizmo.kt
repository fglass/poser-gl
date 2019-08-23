package gizmo

import org.joml.*
import org.liquidengine.legui.style.color.ColorUtil
import render.Loader
import render.RenderContext
import shader.GizmoShader
import kotlin.math.ceil
import kotlin.math.floor

class TranslationGizmo(loader: Loader, shader: GizmoShader): Gizmo(shader) { // TODO: chain to node position

    override var scale = 40f
    override var model = getModel("translation", loader)
    override var axes = arrayOf(
        GizmoAxis(AxisType.X, ColorUtil.fromInt(220, 14, 44, 1f), Vector3f(0f, 180f, 0f)),
        GizmoAxis(AxisType.Y, ColorUtil.fromInt(14, 220, 44, 1f), Vector3f(0f, 0f, -90f)),
        GizmoAxis(AxisType.Z, ColorUtil.fromInt(14, 44, 220, 1f), Vector3f(0f, 90f, 0f))
    )

    override fun getClosestAxis(ray: Rayf): GizmoAxis? {
        var minDistance = Float.MAX_VALUE
        var closest: GizmoAxis? = null

        for (axis in axes) {
            val offset = Vector3f(2f)
            val min = Vector3f(position).sub(Vector3f(offset).setComponent(axis.type.ordinal, scale))
            val max = Vector3f(position).add(Vector3f(offset).setComponent(axis.type.ordinal, 0f))
            val nearFar = Vector2f()

            if (Intersectionf.intersectRayAab(ray, AABBf(min, max), nearFar) && nearFar.x < minDistance) {
                minDistance = nearFar.x
                closest = axis
            }
        }
        return closest
    }

    override fun manipulate(context: RenderContext, ray: Rayf) {
        selectedAxis?.let {
            val intersection = getPlaneIntersection(ray)
            if (it.previousIntersection != Vector3f() && intersection != it.previousIntersection) {
                val delta = Vector3f(intersection).sub(it.previousIntersection).get(it.type.ordinal)
                transform(it, context, delta)
            }
            it.previousIntersection = intersection
        }
    }

    private fun transform(axis: GizmoAxis, context: RenderContext, delta: Float) {
        var value = if (delta > 0) ceil(delta) else floor(delta)
        value = if (axis.type == AxisType.X) -value else value
        context.gui.editorPanel.sliders[axis.type.ordinal].adjust(value.toInt())
    }
}