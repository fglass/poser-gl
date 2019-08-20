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
import kotlin.math.acos

class RotationGizmo(loader: Loader, private val shader: GizmoShader): Gizmo() {

    private val scale = 25f
    private val model: Model = getModel("rotation", loader)
    private val axes = arrayOf(
        GizmoAxis(AxisType.X, ColorUtil.fromInt(220, 14, 44, 1f), Vector3f(0f, 0f, 90f)),
        GizmoAxis(AxisType.Y, ColorUtil.fromInt(14, 220, 44, 1f), Vector3f(0f, 0f, 0f)),
        GizmoAxis(AxisType.Z, ColorUtil.fromInt(14, 44, 220, 1f), Vector3f(-90f, 0f, 0f))
    )

    override fun render(context: RenderContext, viewMatrix: Matrix4f, ray: Rayf) {
        prepare(context, model, shader, viewMatrix)

        when {
            !active -> selectAxis(getClosestAxis(ray))
            else -> manipulate(context, ray)
        }

        for (axis in axes) {
            val transformation = MatrixCreator.createTransformationMatrix(position, axis.rotation, scale)
            shader.loadTransformationMatrix(transformation)

            axis.colour.w = if (axis == selectedAxis) 0.6f else 1f // Lower opacity to indicate highlighted axis
            shader.loadColour(axis.colour)
            glDrawArrays(GL_TRIANGLES, 0, model.vertexCount)
        }
    }

    private fun getClosestAxis(ray: Rayf): GizmoAxis? {
        var minDistance = Float.MAX_VALUE
        var closest: GizmoAxis? = null

        for (axis in axes) {
            val offset = Vector3f(scale).setComponent(axis.type.ordinal, 2f)
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

    private fun selectAxis(axis: GizmoAxis?) {
        if (axis != selectedAxis) {
            selectedAxis = axis
            selectedAxis?.previousIntersection = Vector3f() // Reset intersection
        }
    }

    private fun manipulate(context: RenderContext, ray: Rayf) {
        selectedAxis?.let {
            val intersection = getIntersection(ray)
            if (it.previousIntersection != Vector3f()) {
                val product = intersection.dot(it.previousIntersection)
                val theta = acos(product)
                val angle = Math.toDegrees(theta.toDouble())

                if (angle.isFinite()) {
                    val normal = Vector3f(-ray.dX, -ray.dY, -ray.dZ)
                    val numerator = Vector3f(intersection).cross(it.previousIntersection).mul(normal)
                    val length = numerator.length()
                    val sign = numerator.div(length)[it.type.ordinal]
                    transform(context, angle, sign < 0)
                }
            }
            it.previousIntersection = intersection
        }
    }

    private fun getIntersection(ray: Rayf): Vector3f {
        // Allow for transforming without need to hover over axis
        val plane = Planef(Vector3f(position), Vector3f(-ray.dX, -ray.dY, -ray.dZ))
        val epsilon = Intersectionf.intersectRayPlane(ray, plane, 0f)

        // Origin + direction * epsilon
        val onPlane = Vector3f(ray.oX, ray.oY, ray.oZ).add(Vector3f(ray.dX, ray.dY, ray.dZ).mul(epsilon))

        // Project point onto circle's circumference
        val r = scale / 2
        val p = Vector3f(onPlane).sub(position).normalize().mul(r)
        val point = Vector3f(position).add(p)
        return Vector3f(onPlane).sub(point).normalize()
    }

    private fun transform(context: RenderContext, delta: Double, negative: Boolean) {
        val axis = selectedAxis?: return
        axes.forEach {
            var ord = axis.type.ordinal
            if (it.type == AxisType.Z) { // TODO
                ord = when (axis.type) {
                    AxisType.Y -> ord + 1
                    AxisType.Z -> ord - 1
                    else -> ord
                }
            }

            val offset = (if (negative) -delta else delta).toFloat()
            val value = it.rotation[ord] + offset
            it.rotation.setComponent(ord, value)
        }
    }

    fun reset() {
        axes.forEach(GizmoAxis::reset)
    }
}