package render

import entity.Entity
import entity.Light
import org.joml.Matrix4f
import org.joml.Vector3f
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30
import shader.StaticShader
import util.Maths
import org.joml.Vector2f
import kotlin.math.tan

const val FOV = 70f
const val NEAR_PLANE = 1f
const val FAR_PLANE = 10000f

class EntityRenderer(private val shader: StaticShader) {

    lateinit var projectionMatrix: Matrix4f

    fun init(fboSize: Vector2f, loadLight: Boolean) {
        shader.start()
        if (loadLight) {
            shader.loadLight(Light(Vector3f(0f, -500f, -1000f), Vector3f(1f, 1f, 1f)))
        }
        projectionMatrix = createProjectionMatrix(fboSize)
        shader.loadProjectionMatrix(projectionMatrix)
        shader.stop()
    }

    private fun createProjectionMatrix(fboSize: Vector2f): Matrix4f {
        val aspectRatio = fboSize.x / fboSize.y
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

    fun render(entity: Entity?, shader: StaticShader) {
        if (entity == null) {
            return
        }

        GL30.glBindVertexArray(entity.model.vaoId)
        GL20.glEnableVertexAttribArray(0)
        GL20.glEnableVertexAttribArray(1)

        val transformationMatrix = Maths.createTransformationMatrix(entity.position, entity.rotation, entity.scale)
        shader.loadTransformationMatrix(transformationMatrix)

        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, entity.model.vertexCount)
        GL20.glDisableVertexAttribArray(0)
        GL20.glDisableVertexAttribArray(1)
        GL30.glBindVertexArray(0)
    }
}