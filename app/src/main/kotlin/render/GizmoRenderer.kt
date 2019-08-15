package render

import entity.Camera
import entity.ENTITY_POS
import entity.ENTITY_ROT
import gizmo.GizmoLoader
import model.Model
import shader.GizmoShader
import org.lwjgl.opengl.GL11.GL_TRIANGLES
import org.lwjgl.opengl.GL11.glDrawArrays
import org.lwjgl.opengl.GL30
import util.MatrixCreator

class GizmoRenderer(private val context: RenderContext) {

    private val loader = Loader()
    private val shader = GizmoShader()
    private val translation: Model

    init {
        translation = GizmoLoader.load("translation", loader)
    }

    fun render(camera: Camera) {
        prepare(translation, camera)
        glDrawArrays(GL_TRIANGLES, 0, translation.vertexCount)
        finish()
    }

    private fun prepare(gizmo: Model, camera: Camera) {
        shader.start()
        GL30.glBindVertexArray(gizmo.vaoId)
        GL30.glEnableVertexAttribArray(0)
        GL30.glDisable(GL30.GL_DEPTH_TEST)

        shader.loadTransformationMatrix(MatrixCreator.createTransformationMatrix(ENTITY_POS, ENTITY_ROT, 75f))
        shader.loadProjectionMatrix(context.entityRenderer.projectionMatrix)
        shader.loadViewMatrix(camera)
    }

    private fun finish() {
        GL30.glEnable(GL30.GL_DEPTH_TEST)
        GL30.glDisableVertexAttribArray(0)
        GL30.glBindVertexArray(0)
        shader.stop()
    }

    fun cleanUp() {
        loader.cleanUp()
        shader.cleanUp()
    }
}