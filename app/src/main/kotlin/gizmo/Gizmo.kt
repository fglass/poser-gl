package gizmo

import entity.Camera
import model.Model
import org.joml.*
import org.liquidengine.legui.input.Mouse
import org.liquidengine.legui.style.color.ColorUtil
import org.lwjgl.opengl.GL11.GL_TRIANGLES
import org.lwjgl.opengl.GL11.glDrawArrays
import org.lwjgl.opengl.GL20.glEnableVertexAttribArray
import org.lwjgl.opengl.GL30.glBindVertexArray
import render.Loader
import render.RenderContext
import shader.GizmoShader
import util.MatrixCreator
import kotlin.math.ceil

/**
 * https://nelari.us/post/gizmos/
 */
class Gizmo(type: String, loader: Loader, private val shader: GizmoShader) { // TODO: refactor

    private val model: Model = GizmoLoader.load(type, loader)
    private val axes = 3
    private var previousAxis = -1
    private var previousIntersection = Vector3f(0f)

    var active = false
    var position = Vector3f(0f, 0f, 0f)
    private val rotations = arrayOf(Vector3f(0f, 180f, 0f), Vector3f(0f, 0f, -90f), Vector3f(0f, 90f, 0f))
    private val scale = 40f
    private val colours = arrayOf(
        ColorUtil.fromInt(220, 14, 44, 1f), // Red
        ColorUtil.fromInt(14, 220, 44, 1f), // Green
        ColorUtil.fromInt(14, 44, 220, 1f)  // Blue
    )

    fun render(context: RenderContext, camera: Camera) {
        glBindVertexArray(model.vaoId)
        glEnableVertexAttribArray(0)

        val viewMatrix = MatrixCreator.createViewMatrix(camera)
        shader.loadViewMatrix(viewMatrix)
        shader.loadProjectionMatrix(context.entityRenderer.projectionMatrix)

        val (selectedAxis, ray, t) = getClosestAxis(context, viewMatrix)
        if (selectedAxis != -1) {

            if (!active && selectedAxis != previousAxis) { // TODO: axis class/enum
                previousAxis = selectedAxis
                previousIntersection = Vector3f(0f)
            }

            if (active) {
                val intersection = getIntersection(ray, t)
                if (previousIntersection != Vector3f(0f)) {
                    val delta = Vector3f(intersection).sub(previousIntersection)
                    transform(delta, context)
                }
                previousIntersection = intersection
            }
        }

        repeat(axes) {
            val transformation = MatrixCreator.createTransformationMatrix(position, rotations[it], scale)
            shader.loadTransformationMatrix(transformation)

            val opacity = if (it != selectedAxis) 1f else 0.6f
            shader.loadColour(Vector4f(colours[it].x, colours[it].y, colours[it].z, opacity))
            glDrawArrays(GL_TRIANGLES, 0, model.vertexCount)
        }
    }

    private fun getClosestAxis(context: RenderContext, viewMatrix: Matrix4f): Triple<Int, Rayf, Float> {
        val ray = calculateRay(context, viewMatrix)
        var minDistance = Float.MAX_VALUE
        var closest = -1

        repeat(axes) {
            val offset = Vector3f(2f) // TODO: elongate for smoother manipulation or set min distance
            val min = Vector3f(position).sub(Vector3f(offset).setComponent(it, scale))
            val max = Vector3f(position).add(Vector3f(offset).setComponent(it, 0f))
            val nearFar = Vector2f()

            if (Intersectionf.intersectRayAab(ray, AABBf(min, max), nearFar) && nearFar.x < minDistance) {
                minDistance = nearFar.x
                closest = it
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

    private fun getIntersection(ray: Rayf, t: Float): Vector3f {
        // p(t) = origin + dir * t
        return Vector3f(ray.oX, ray.oY, ray.oZ).add(Vector3f(ray.dX, ray.dY, ray.dZ).mul(t))
    }

    private fun transform(delta: Vector3f, context: RenderContext) {
        var offset = delta[previousAxis]
        //position.setComponent(previousAxis, position[previousAxis] + offset) // TODO: remove?

        if (previousAxis == 0) { // Inverse for x axis
            offset *= -1
        }
        val current = context.gui.editorPanel.sliders[previousAxis].getValue()
        val newValue = ceil(current + offset).toInt()

        context.gui.editorPanel.sliders[previousAxis].setValue(newValue) // TODO: limit
        context.animationHandler.transformNode(previousAxis, newValue)
    }

    fun endTransform() {
        active = false
        previousAxis = -1
    }
}