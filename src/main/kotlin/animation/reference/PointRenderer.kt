package animation.reference

import entity.Camera
import entity.Entity
import model.Model
import org.joml.Matrix4f
import org.joml.Vector3f
import org.lwjgl.opengl.GL30.*
import render.Loader
import utils.Maths

class PointRenderer(projectionMatrix: Matrix4f) {

    private val quad: Model
    private val loader = Loader()
    private val shader = ReferenceShader()
    private val points = ArrayList<ReferencePoint>()

    init {
        val vertices = floatArrayOf(-0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.5f, -0.5f)
        quad = loader.loadToVao(vertices)
        shader.start()
        shader.loadProjectionMatrix(projectionMatrix)
        shader.stop()
    }

    fun render(entity: Entity?, camera: Camera) {
        if (entity == null) {
            return
        }

        if (points.isEmpty()) {
            addPoints(entity)
        }

        prepare()
        for (point in points) {
            val viewMatrix = Maths.createViewMatrix(camera)
            updateMatrix(point.position, point.rotation, point.scale, viewMatrix)
            glDrawArrays(GL_TRIANGLE_STRIP, 0, quad.vertexCount)
        }
        finish()
    }

    private fun addPoints(entity: Entity) {
        val def = entity.model.definition
        for (i in 0 until def.vertexPositionsX.size) {
            points.add(
                ReferencePoint(Vector3f(
                    def.vertexPositionsX[i].toFloat(),
                    def.vertexPositionsY[i].toFloat(),
                    def.vertexPositionsZ[i].toFloat()), 0f, 1f
                )
            )
        }
    }

    private fun prepare() {
        shader.start()
        glBindVertexArray(quad.vaoId)
        glEnableVertexAttribArray(0)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
    }

    private fun updateMatrix(position: Vector3f, rotation: Float, scale: Float, viewMatrix: Matrix4f) {
        val modelMatrix = Matrix4f()
        modelMatrix.translate(position)
        modelMatrix.m00(viewMatrix.m00())
        modelMatrix.m01(viewMatrix.m10())
        modelMatrix.m02(viewMatrix.m20())
        modelMatrix.m10(viewMatrix.m01())
        modelMatrix.m11(viewMatrix.m11())
        modelMatrix.m12(viewMatrix.m21())
        modelMatrix.m20(viewMatrix.m02())
        modelMatrix.m21(viewMatrix.m12())
        modelMatrix.m22(viewMatrix.m22())
        modelMatrix.rotate(Math.toRadians(rotation.toDouble()).toFloat(), Vector3f(0f, 0f, 1f))
        modelMatrix.scale(scale)
        shader.loadModelViewMatrix(viewMatrix.mul(modelMatrix))
    }

    private fun finish() {
        glDisable(GL_BLEND)
        glDisableVertexAttribArray(0)
        glBindVertexArray(0)
        shader.stop()
    }

    fun cleanUp() {
        loader.cleanUp()
        shader.cleanUp()
    }
}