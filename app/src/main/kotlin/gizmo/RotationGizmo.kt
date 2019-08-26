package gizmo

import model.Model
import org.joml.*
import org.liquidengine.legui.style.color.ColorUtil
import org.lwjgl.opengl.GL11.GL_TRIANGLES
import org.lwjgl.opengl.GL11.glDrawArrays
import render.Loader
import render.RenderContext
import shader.GizmoShader
import util.MatrixCreator
import java.lang.Math.toDegrees
import java.lang.Math.toRadians
import kotlin.math.*

const val ROTATION_SPEED = 2

class RotationGizmo(private val context: RenderContext, loader: Loader, shader: GizmoShader): Gizmo(context, shader) {

    override var scale = 25f
    override var model: Model = getModel("rotation", loader)
    override var axes = arrayOf(
        GizmoAxis(AxisType.X, ColorUtil.fromInt(220, 14, 44, 1f), Vector3f(0f, 0f, 90f)),
        GizmoAxis(AxisType.Y, ColorUtil.fromInt(14, 220, 44, 1f), Vector3f(0f, 0f, 0f)),
        GizmoAxis(AxisType.Z, ColorUtil.fromInt(14, 44, 220, 1f), Vector3f(0f, 90f, 90f))
    )

    override fun getClosestAxis(ray: Rayf): GizmoAxis? {
        var minDistance = Float.MAX_VALUE
        var closest: GizmoAxis? = null

        for (axis in axes) {
            val offset = Vector3f(getRelativeScale()).setComponent(axis.type.ordinal, 2f)
            val min = Vector3f(position).sub(offset)
            val max = Vector3f(position).add(offset)
            val nearFar = Vector2f()

            if (Intersectionf.intersectRayAab(ray, AABBf(min, max), nearFar) && nearFar.x < minDistance) {
                minDistance = nearFar.x
                closest = axis
            }
        }
        return closest
    }

    override fun manipulate(ray: Rayf) {
        selectedAxis?.let {
            val intersection = getCircleIntersection(ray) // TODO: torus intersection (4x box)
            if (it.previousIntersection != Vector3f() && intersection != it.previousIntersection) {
                val cross = Vector3f(intersection).cross(it.previousIntersection)
                val sin = cross.length() / (intersection.length() * it.previousIntersection.length())
                val theta = asin(sin)
                val delta = toDegrees(theta.toDouble())
                transform(it, delta, cross[it.type.ordinal] > 0)
            }
            it.previousIntersection = intersection
        }
    }

    private fun getCircleIntersection(ray: Rayf): Vector3f {
        val onPlane = getPlaneIntersection(ray)
        // Project point onto circle's circumference
        val p = Vector3f(onPlane).sub(position).normalize().mul(getRelativeScale())
        val point = Vector3f(position).add(p)
        return Vector3f(onPlane).sub(point).normalize()
    }

    private fun transform(axis: GizmoAxis, delta: Double, negative: Boolean) {
        var value = ceil(delta).toInt()
        value = if (negative) -value else value
        value = if (axis.type == AxisType.Y) -value else value
        value = value.coerceIn(-1, 1) * ROTATION_SPEED
        context.gui.editorPanel.sliders[axis.type.ordinal].adjust(value, true)
    }

    fun reset() {
        axes.forEach(GizmoAxis::reset)
    }
}