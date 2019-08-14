package render

import gizmo.GizmoLoader
import model.Model
import shader.StaticShader
import org.lwjgl.opengl.GL11.GL_TRIANGLES
import org.lwjgl.opengl.GL11.glDrawArrays
import org.lwjgl.opengl.GL30

class GizmoRenderer {

    private val loader = Loader()
    private val shader = StaticShader()
    private val translation: Model

    init {
        translation = GizmoLoader.load("translation", loader)
    }

    fun render() {
        prepare(translation)
        glDrawArrays(GL_TRIANGLES, 0, translation.vertexCount)
        finish()
    }

    private fun prepare(gizmo: Model) {
        shader.start()
        GL30.glBindVertexArray(gizmo.vaoId)
        GL30.glEnableVertexAttribArray(0)
        GL30.glDisable(GL30.GL_DEPTH_TEST)
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