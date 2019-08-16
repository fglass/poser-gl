package gizmo

import entity.Camera
import model.Model
import org.joml.*
import org.liquidengine.legui.input.Mouse
import org.liquidengine.legui.style.color.ColorUtil
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30
import render.Loader
import render.RenderContext
import shader.GizmoShader
import util.MatrixCreator
import kotlin.math.ceil

class Gizmo(type: String, loader: Loader, private val shader: GizmoShader) {

    private val model: Model = GizmoLoader.load(type, loader)
    private val axes = 3
    private var selectedAxis = -1
    private var previousAxis = -1

    var position = Vector3f(0f, 0f, 0f)
    private val rotations = arrayOf(Vector3f(0f, 180f, 0f), Vector3f(0f, 0f, -90f), Vector3f(0f, 90f, 0f))
    private val scale = 40f
    private val colours = arrayOf(
        ColorUtil.fromInt(220, 14, 44, 1f), // Red
        ColorUtil.fromInt(14, 220, 44, 1f), // Green
        ColorUtil.fromInt(14, 44, 220, 1f)  // Blue
    )

    fun render(context: RenderContext, camera: Camera) {
        GL30.glBindVertexArray(model.vaoId)
        GL30.glEnableVertexAttribArray(0)

        val viewMatrix = MatrixCreator.createViewMatrix(camera)
        shader.loadViewMatrix(viewMatrix)
        shader.loadProjectionMatrix(context.entityRenderer.projectionMatrix)

        selectedAxis = getClosestAxis(context, viewMatrix)
        if (selectedAxis != -1) {
            previousAxis = selectedAxis
        }

        repeat(axes) {
            val transformation = MatrixCreator.createTransformationMatrix(position, rotations[it], scale)
            shader.loadTransformationMatrix(transformation)

            val opacity = if (it != selectedAxis) 1f else 0.6f
            shader.loadColour(Vector4f(colours[it].x, colours[it].y, colours[it].z, opacity))
            GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, model.vertexCount)
        }
    }

    private fun getClosestAxis(context: RenderContext, viewMatrix: Matrix4f): Int {
        val ray = calculateRay(context, viewMatrix)
        var minDistance = Float.MAX_VALUE
        var closest = -1

        repeat(axes) {
            val offset = Vector3f(2f, 2f, 2f)
            val min = Vector3f(position).sub(Vector3f(offset).setComponent(it, scale))
            val max = Vector3f(position).add(Vector3f(offset).setComponent(it, 0f))
            val nearFar = Vector2f()

            if (Intersectionf.intersectRayAab(ray, AABBf(min, max), nearFar) && nearFar.x < minDistance) {
                minDistance = nearFar.x
                closest = it
            }
        }
        return closest
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

    fun transform(delta: Vector2f, context: RenderContext) {
        if (previousAxis == 0 || previousAxis == 1) {
            val offset = delta[previousAxis]

            val current = context.gui.editorPanel.sliders[previousAxis].getValue()
            val newValue = ceil(current + offset).toInt()

            context.gui.editorPanel.sliders[previousAxis].setValue(newValue) // TODO: limit
            context.animationHandler.transformNode(previousAxis, newValue)

            //offset *= if (previousAxis == 0) -1 else 1
            //position.setComponent(previousAxis, position[previousAxis] + offset)
        }
    }

    fun endTransform() {
        previousAxis = -1
    }
}