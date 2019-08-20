package gizmo

import model.Model
import org.joml.*
import org.liquidengine.legui.style.color.ColorUtil
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.glDrawArrays
import render.Loader
import render.RenderContext
import shader.GizmoShader
import util.MatrixCreator
import kotlin.math.ceil

class TranslationGizmo(loader: Loader, private val shader: GizmoShader): Gizmo() {

    private val scale = 40f
    private val model: Model = getModel("translation", loader)
    private val axes = arrayOf(
        GizmoAxis(AxisType.X, ColorUtil.fromInt(220, 14, 44, 1f), Vector3f(0f, 180f, 0f)),
        GizmoAxis(AxisType.Y, ColorUtil.fromInt(14, 220, 44, 1f), Vector3f(0f, 0f, -90f)),
        GizmoAxis(AxisType.Z, ColorUtil.fromInt(14, 44, 220, 1f), Vector3f(0f, 90f, 0f))
    )

    override fun render(context: RenderContext, viewMatrix: Matrix4f, ray: Rayf) {
        prepare(context, model, shader, viewMatrix)

        when {
            !active -> selectAxis(getClosestAxis(ray))
            else -> manipulate(context, ray)
        }

        // Render gizmo axes
        for (axis in axes) {
            val transformation = MatrixCreator.createTransformationMatrix(position, axis.rotation, scale)
            shader.loadTransformationMatrix(transformation)

            axis.colour.w = if (axis == selectedAxis) 0.6f else 1f // Lower opacity to indicate highlighted axis
            shader.loadColour(axis.colour)
            glDrawArrays(GL11.GL_TRIANGLES, 0, model.vertexCount)
        }
    }

    private fun getClosestAxis(ray: Rayf): GizmoAxis? {
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
                val delta = Vector3f(intersection).sub(it.previousIntersection).get(it.type.ordinal)
                transform(context, delta)
            }
            it.previousIntersection = intersection
        }
    }

    private fun getIntersection(ray: Rayf): Vector3f {
        // Allow for transforming without need to hover over axis
        val plane = Planef(Vector3f(position), Vector3f(-ray.dX, -ray.dY, -ray.dZ))
        val epsilon = Intersectionf.intersectRayPlane(ray, plane, 0f)

        // origin + dir * epsilon
        return Vector3f(ray.oX, ray.oY, ray.oZ).add(Vector3f(ray.dX, ray.dY, ray.dZ).mul(epsilon))
    }

    private fun transform(context: RenderContext, delta: Float) {
        val axis = selectedAxis?: return
        //position.setComponent(axis.type.ordinal, position[axis.type.ordinal] + delta) // TODO: need?

        val value = ceil(if (axis.type == AxisType.X) -delta else delta).toInt()
        context.gui.editorPanel.sliders[axis.type.ordinal].adjust(value)
    }
}