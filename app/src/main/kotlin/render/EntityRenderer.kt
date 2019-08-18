package render

import entity.Camera
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
import kotlin.math.tan

const val FOV = 70f
const val NEAR_PLANE = 1f
const val FAR_PLANE = 10000f

class EntityRenderer {

    private val shader = StaticShader()
    lateinit var projectionMatrix: Matrix4f

    fun init(width: Int, height: Int) {
        shader.start()
        shader.loadLight(Light(Vector3f(0f, -500f, -1000f), Vector3f(1f, 1f, 1f)))
        projectionMatrix = createProjectionMatrix(width, height)
        shader.loadProjectionMatrix(projectionMatrix)
        shader.stop()
    }

    private fun createProjectionMatrix(width: Int, height: Int): Matrix4f {
        val aspectRatio = width.toFloat() / height
        val yScale = ((1f / tan(Math.toRadians((FOV / 2f).toDouble()))) * aspectRatio).toFloat()
        val xScale = yScale / aspectRatio
        val frustumLength = FAR_PLANE - NEAR_PLANE

        val projectionMatrix = Matrix4f()
        projectionMatrix.m00(xScale)
        projectionMatrix.m11(yScale)
        projectionMatrix.m22(-((FAR_PLANE + NEAR_PLANE) / frustumLength))
        projectionMatrix.m23(-1f)
        projectionMatrix.m32(-((2 * NEAR_PLANE * FAR_PLANE) / frustumLength))
        projectionMatrix.m33(0f)
        return projectionMatrix
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