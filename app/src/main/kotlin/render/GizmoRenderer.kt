package render

import entity.Camera
import gizmo.GizmoLoader
import model.Model
import org.joml.Vector3f
import org.liquidengine.legui.style.color.ColorUtil
import shader.GizmoShader
import org.lwjgl.opengl.GL11.GL_TRIANGLES
import org.lwjgl.opengl.GL11.glDrawArrays
import org.lwjgl.opengl.GL30.*
import util.MatrixCreator

class GizmoRenderer(private val context: RenderContext) {

    private val loader = Loader()
    private val shader = GizmoShader()
    private val translation = Gizmo("translation", loader, shader)

    fun render(camera: Camera) {
        prepare()
        translation.render(context, camera)
        finish()
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
        private val position = Vector3f(0f, -100f, 0f)
        private val rotations = arrayOf(Vector3f(0f, 180f, 0f), Vector3f(0f, 0f, -90f), Vector3f(0f, 90f, 0f))
        private val colours = arrayOf(
            ColorUtil.fromInt(220, 14, 44, 1f), // Red
            ColorUtil.fromInt(14, 220, 44, 1f), // Green
            ColorUtil.fromInt(14, 44, 220, 1f)  // Blue
        )

        fun render(context: RenderContext, camera: Camera) {
            glBindVertexArray(model.vaoId)
            glEnableVertexAttribArray(0)
            shader.loadProjectionMatrix(context.entityRenderer.projectionMatrix)
            shader.loadViewMatrix(camera)

            repeat(axes) {
                val transformation = MatrixCreator.createTransformationMatrix(position, rotations[it], 50f)
                shader.loadTransformationMatrix(transformation)
                shader.loadColour(Vector3f(colours[it].x, colours[it].y, colours[it].z))
                glDrawArrays(GL_TRIANGLES, 0, model.vertexCount)
            }
        }
    }
}