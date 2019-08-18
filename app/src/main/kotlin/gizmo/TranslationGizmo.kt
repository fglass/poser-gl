package gizmo

import entity.Camera
import model.Model
import org.joml.*
import org.liquidengine.legui.input.Mouse
import org.liquidengine.legui.style.color.ColorUtil
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30
import render.Loader
import render.RenderContext
import shader.GizmoShader
import util.MatrixCreator
import kotlin.math.ceil

class TranslationGizmo(loader: Loader, private val shader: GizmoShader): Gizmo() {

    private val scale = 40f
    private val model: Model = GizmoLoader.load("translation", loader)
    private val axes = arrayOf(
        GizmoAxis(AxisType.X, ColorUtil.fromInt(220, 14, 44, 1f), Vector3f(0f, 180f, 0f)),
        GizmoAxis(AxisType.Y, ColorUtil.fromInt(14, 220, 44, 1f), Vector3f(0f, 0f, -90f)),
        GizmoAxis(AxisType.Z, ColorUtil.fromInt(14, 44, 220, 1f), Vector3f(0f, 90f, 0f))
    )

    override fun render(context: RenderContext, camera: Camera) {
        GL30.glBindVertexArray(model.vaoId)
        GL20.glEnableVertexAttribArray(0)

        val viewMatrix = MatrixCreator.createViewMatrix(camera)
        shader.loadViewMatrix(viewMatrix)
        shader.loadProjectionMatrix(context.entityRenderer.projectionMatrix)

        val (closestAxis, ray, t) = getClosestAxis(context, viewMatrix)
        if (closestAxis != null) {
            handleAxis(closestAxis, ray, t, context)
        }

        for (axis in axes) {
            val transformation = MatrixCreator.createTransformationMatrix(position, axis.rotation, scale)
            shader.loadTransformationMatrix(transformation)

            axis.colour.w = if (axis != selectedAxis) 1f else 0.6f // Lower opacity to indicate highlighted axis
            shader.loadColour(axis.colour)
            GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, model.vertexCount)
        }
    }

    private fun getClosestAxis(context: RenderContext, viewMatrix: Matrix4f): Triple<GizmoAxis?, Rayf, Float> {
        val ray = calculateRay(context, viewMatrix)
        var minDistance = Float.MAX_VALUE
        var closest: GizmoAxis? = null

        for (axis in axes) {
            val offset = Vector3f(2f) // TODO: elongate for smoother manipulation or set min distance when active
            val min = Vector3f(position).sub(Vector3f(offset).setComponent(axis.type.index, scale))
            val max = Vector3f(position).add(Vector3f(offset).setComponent(axis.type.index, 0f))
            val nearFar = Vector2f()

            if (Intersectionf.intersectRayAab(ray, AABBf(min, max), nearFar) && nearFar.x < minDistance) {
                minDistance = nearFar.x
                closest = axis
            }
        }
        return Triple(closest, ray, minDistance)
    }

    private fun calculateRay(context: RenderContext, viewMatrix: Matrix4f): Rayf { // TODO deduplicate
        val mousePosition = Mouse.getCursorPosition()
        mousePosition.sub(context.framebuffer.position)

        val origin = Vector3f()
        val dir = Vector3f()
        Matrix4f(context.entityRenderer.projectionMatrix)
            .mul(viewMatrix)
            .unprojectRay(mousePosition.x, mousePosition.y, intArrayOf(0, 0,
                context.framebuffer.size.x.toInt(), context.framebuffer.size.y.toInt()), origin, dir
            )
        return Rayf(origin, dir)
    }

    private fun handleAxis(closestAxis: GizmoAxis, ray: Rayf, t: Float, context: RenderContext) { // TODO: refactor
        if (!active && closestAxis != selectedAxis) { // Axis changed
            closestAxis.previousIntersection = Vector3f(0f)
            selectedAxis = closestAxis
        }

        if (active) {
            val intersection = getIntersection(ray, t)
            val axis = selectedAxis?: return

            if (axis.previousIntersection != Vector3f(0f)) {
                val delta = Vector3f(intersection).sub(axis.previousIntersection)
                transform(delta, context)
            }
            axis.previousIntersection = intersection
        }
    }

    private fun getIntersection(ray: Rayf, t: Float): Vector3f {
        // p(t) = origin + dir * t
        return Vector3f(ray.oX, ray.oY, ray.oZ).add(Vector3f(ray.dX, ray.dY, ray.dZ).mul(t))
    }

    private fun transform(delta: Vector3f, context: RenderContext) {
        val axis = selectedAxis?: return
        var offset = delta[axis.type.index]
        //position.setComponent(previousAxis, position[previousAxis] + offset) // TODO: need?

        if (axis.type == AxisType.X) { // Inverse for x axis
            offset *= -1
        }
        val current = context.gui.editorPanel.sliders[axis.type.index].getValue()
        val newValue = ceil(current + offset).toInt()

        context.gui.editorPanel.sliders[axis.type.index].setValue(newValue) // TODO: limit
        context.animationHandler.transformNode(axis.type.index, newValue)
    }

    override fun endTransform() {
        active = false
        selectedAxis = null
    }
}