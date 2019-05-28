package render

import entity.Entity
import org.joml.Matrix4f
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30
import shader.StaticShader
import utils.Maths

const val FOV = 70F
const val NEAR_PLANE = 0.1F
const val FAR_PLANE = 1000F

class Renderer(shader: StaticShader) {

    private lateinit var projectionMatrix: Matrix4f

    init {
        createProjectionMatrix()
        shader.start()
        shader.loadProjectionMatrix(projectionMatrix)
        shader.stop()
    }

    fun render(entity: Entity, shader: StaticShader) {
        val model = entity.rawModel
        GL30.glBindVertexArray(model.vaoId)
        GL20.glEnableVertexAttribArray(0)
        GL20.glEnableVertexAttribArray(1)

        val transformationMatrix = Maths.createTransformationMatrix(
            entity.position, entity.rotation, entity.scale
        )
        shader.loadTransformationMatrix(transformationMatrix)

        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, model.vertexCount)
        GL20.glDisableVertexAttribArray(0)
        GL20.glDisableVertexAttribArray(1)
        GL30.glBindVertexArray(0)
    }

    private fun createProjectionMatrix() {
        val aspectRatio = 762f / 503f // TODO
        val yScale = ((1f / Math.tan(Math.toRadians((FOV / 2f).toDouble()))) * aspectRatio).toFloat()
        val xScale = yScale / aspectRatio
        val frustumLength = FAR_PLANE - NEAR_PLANE

        projectionMatrix = Matrix4f()
        projectionMatrix.m00(xScale)
        projectionMatrix.m11(yScale)
        projectionMatrix.m22(-((FAR_PLANE + NEAR_PLANE) / frustumLength))
        projectionMatrix.m23(-1f)
        projectionMatrix.m32(-((2 * NEAR_PLANE * FAR_PLANE) / frustumLength))
        projectionMatrix.m33(0f)
    }
}