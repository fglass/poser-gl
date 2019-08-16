package render

import animation.ReferenceNode
import animation.TransformationType
import entity.Camera
import gizmo.GizmoLoader
import model.Model
import org.joml.*
import org.liquidengine.legui.input.Mouse
import org.liquidengine.legui.style.color.ColorUtil
import shader.GizmoShader
import org.lwjgl.opengl.GL11.GL_TRIANGLES
import org.lwjgl.opengl.GL11.glDrawArrays
import org.lwjgl.opengl.GL30.*
import util.MatrixCreator

class GizmoRenderer(private val context: RenderContext) {

    private var enabled = false
    private val loader = Loader()
    private val shader = GizmoShader()
    private val translation = Gizmo("translation", loader, shader)

    fun enable(node: ReferenceNode, type: TransformationType) {
        translation.position = node.position
        enabled = true
    }

    fun render(camera: Camera) {
        if (enabled) {
            prepare()
            translation.render(context, camera)
            finish()
        }
    }

    private fun prepare() {
        shader.start()
        glDisable(GL_DEPTH_TEST)
    }

    private fun finish() {
        glEnable(GL_DEPTH_TEST)
        glDisableVertexAttribArray(0)
        glBindVertexArray(0)
        shader.stop()
    }

    fun cleanUp() {
        loader.cleanUp()
        shader.cleanUp()
    }

    class Gizmo(type: String, loader: Loader, private val shader: GizmoShader) {

        private val model: Model = GizmoLoader.load(type, loader)
        private val axes = 3

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
            val closest = getClosestAxis(context, viewMatrix)

            repeat(axes) {
                val transformation = MatrixCreator.createTransformationMatrix(position, rotations[it], scale)
                shader.loadTransformationMatrix(transformation)

                val opacity = if (it != closest) 1f else 0.6f
                shader.loadColour(Vector4f(colours[it].x, colours[it].y, colours[it].z, opacity))
                glDrawArrays(GL_TRIANGLES, 0, model.vertexCount)
            }
        }

        private fun getClosestAxis(context: RenderContext, viewMatrix: Matrix4f): Int {
            val ray = calculateRay(context, viewMatrix)
            var minDistance = Float.MAX_VALUE
            var closest = -1

            repeat(axes) {
                val s = when (it) {
                    0 -> Vector3f(scale, 2f, 2f)
                    1 -> Vector3f(2f, scale, 2f)
                    else -> Vector3f(2f, 2f, scale)
                }

                val min = Vector3f(position).sub(s)
                val max = Vector3f(position)
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
    }
}