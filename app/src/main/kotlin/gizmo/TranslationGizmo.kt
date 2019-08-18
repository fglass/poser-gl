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
        // Prepare
        GL30.glBindVertexArray(model.vaoId)
        GL20.glEnableVertexAttribArray(0)

        val viewMatrix = MatrixCreator.createViewMatrix(camera)
        shader.loadViewMatrix(viewMatrix)
        shader.loadProjectionMatrix(context.entityRenderer.projectionMatrix)

        // Select closest axis if different
        val ray = calculateRay(context, viewMatrix)
        val closestAxis = getClosestAxis(ray)
        if (!active && closestAxis != selectedAxis) {
            selectAxis(closestAxis)
        }

        // Manipulate gizmo
        if (active) {
            manipulate(context, ray)
        }

        // Render gizmo axes
        for (axis in axes) {
            val transformation = MatrixCreator.createTransformationMatrix(position, axis.rotation, scale)
            shader.loadTransformationMatrix(transformation)

            axis.colour.w = if (axis == selectedAxis) 0.6f else 1f // Lower opacity to indicate highlighted axis
            shader.loadColour(axis.colour)
            GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, model.vertexCount)
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
        selectedAxis = axis
        selectedAxis?.previousIntersection = Vector3f(0f) // Reset intersection
    }

    private fun manipulate(context: RenderContext, ray: Rayf) {
        selectedAxis?.let {
            // origin + dir * epsilon
            val intersection = getIntersection(ray)

            if (intersection.x.isFinite() && it.previousIntersection != Vector3f(0f)) {
                val delta = Vector3f(intersection).sub(it.previousIntersection).get(it.type.ordinal)
                transform(context, delta)
            }
            it.previousIntersection = intersection
        }
    }

    private fun getIntersection(ray: Rayf): Vector3f {
        // Allows for transforming without need to hover over axis
        val plane = Planef(Vector3f(position), Vector3f(-ray.dX, -ray.dY, -ray.dZ))
        val epsilon = Intersectionf.intersectRayPlane(ray, plane, 0f)

        // origin + dir * epsilon
        return Vector3f(ray.oX, ray.oY, ray.oZ).add(Vector3f(ray.dX, ray.dY, ray.dZ).mul(epsilon))
    }

    private fun transform(context: RenderContext, delta: Float) {
        val axis = selectedAxis?: return
        //position.setComponent(axis.type.ordinal, position[axis.type.ordinal] + delta) // TODO: need?

        val current = context.gui.editorPanel.sliders[axis.type.ordinal].getValue()
        val newValue = ceil(current + if (axis.type == AxisType.X) -delta else delta).toInt() // Invert for x axis

        context.gui.editorPanel.sliders[axis.type.ordinal].setValue(newValue) // TODO: limit
        context.animationHandler.transformNode(axis.type.ordinal, newValue)
    }

    override fun endTransform() {
        active = false
        selectedAxis = null
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
}