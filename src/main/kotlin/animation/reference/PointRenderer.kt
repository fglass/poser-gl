package animation.reference

import animation.AnimationHandler
import entity.Camera
import model.Model
import net.runelite.cache.definitions.ModelDefinition
import org.joml.Matrix4f
import org.joml.Vector3f
import org.lwjgl.opengl.GL30.*
import render.Loader
import render.Renderer
import utils.Maths

class PointRenderer(private val glRenderer: Renderer) {

    private val quad: Model
    private val loader = Loader()
    private val shader = ReferenceShader()
    val points = ArrayList<ReferencePoint>()

    init {
        val vertices = floatArrayOf(-0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.5f, -0.5f)
        quad = loader.loadToVao(vertices)
    }

    fun render(camera: Camera) {
        prepare()
        for (point in points) {
            loadMatrices(point.position, point.rotation, point.scale, Maths.createViewMatrix(camera))
            glDrawArrays(GL_TRIANGLE_STRIP, 0, quad.vertexCount)
        }
        finish()
    }

    fun addPoint(def: ModelDefinition, tf: AnimationHandler.Transformation) {
        if (tf.type != 0) {
            return
        }

        var index = 0f
        val offset = Vector3f(tf.dx.toFloat(), tf.dy.toFloat(), tf.dz.toFloat())

        for (i in tf.frameMap) {
            if (i < def.vertexGroups.size) {
                val group = def.vertexGroups[i]
                index += group.size

                for (j in group) {
                    offset.x += def.vertexPositionsX[j]
                    offset.y += def.vertexPositionsY[j]
                    offset.z += def.vertexPositionsZ[j]
                }
            }
        }

        if (index > 0) {
            offset.div(index)
        }
        points.add(ReferencePoint(offset, 0f, 2f))
    }

    private fun prepare() {
        shader.start()
        glBindVertexArray(quad.vaoId)
        glEnableVertexAttribArray(0)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glDisable(GL_DEPTH_TEST)
    }

    private fun loadMatrices(position: Vector3f, rotation: Float, scale: Float, viewMatrix: Matrix4f) {
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
        shader.loadProjectionMatrix(glRenderer.projectionMatrix)
    }

    private fun finish() {
        glDisable(GL_BLEND)
        glEnable(GL_DEPTH_TEST)
        glDisableVertexAttribArray(0)
        glBindVertexArray(0)
        shader.stop()
    }

    fun cleanUp() {
        loader.cleanUp()
        shader.cleanUp()
    }
}