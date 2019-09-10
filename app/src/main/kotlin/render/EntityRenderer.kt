package render

import entity.Entity
import entity.Light
import org.joml.Matrix4f
import org.joml.Vector3f
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.glDrawArrays
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30
import shader.ShadingType
import shader.StaticShader
import util.MatrixCreator

class EntityRenderer(private val context: RenderContext) {

    private val shader = StaticShader()

    init {
        shader.start()
        shader.loadLight(Light(Vector3f(0f, -500f, -1000f), Vector3f(1f, 1f, 1f)))
        shader.stop()
    }

    fun render(entity: Entity?, viewMatrix: Matrix4f, shadingType: ShadingType) {
        if (entity != null) {
            prepare(entity, viewMatrix, shadingType)
            glDrawArrays(GL11.GL_TRIANGLES, 0, entity.model.vertexCount)
            finish()
        }
    }

    private fun prepare(entity: Entity, viewMatrix: Matrix4f, shadingType: ShadingType) {
        shader.start()
        GL30.glBindVertexArray(entity.model.vaoId)
        GL20.glEnableVertexAttribArray(0)
        GL20.glEnableVertexAttribArray(1)

        shader.loadShadingToggle(shadingType != ShadingType.NONE)
        shader.loadViewMatrix(viewMatrix)
        val transformationMatrix = MatrixCreator.createTransformationMatrix(
            entity.position, entity.rotation, entity.scale
        )
        shader.loadTransformationMatrix(transformationMatrix)
        shader.loadProjectionMatrix(context.projectionMatrix)
    }

    private fun finish() {
        GL20.glDisableVertexAttribArray(0)
        GL20.glDisableVertexAttribArray(1)
        GL30.glBindVertexArray(0)
        shader.stop()
    }

    fun cleanUp() {
        shader.cleanUp()
    }
}