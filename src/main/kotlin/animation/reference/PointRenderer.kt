package animation.reference

import animation.AnimationHandler
import entity.Camera
import entity.Entity
import model.Model
import net.runelite.cache.definitions.ModelDefinition
import org.joml.Matrix4f
import org.joml.Vector3f
import org.lwjgl.opengl.GL30.*
import render.Loader
import utils.Maths

class PointRenderer(projectionMatrix: Matrix4f) {

    private val quad: Model
    private val loader = Loader()
    private val shader = ReferenceShader()
    val points = ArrayList<ReferencePoint>()

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
            //addPoint(entity)
        }

        prepare()
        for (point in points) {
            val viewMatrix = Maths.createViewMatrix(camera)
            updateMatrix(point.position, point.rotation, point.scale, viewMatrix)
            glDrawArrays(GL_TRIANGLE_STRIP, 0, quad.vertexCount)
        }
        finish()
    }

    fun addPoint(def: ModelDefinition, transformation: AnimationHandler.Transformation) { // TODO: Clean-up
        if (transformation.fmType != 0) {
            return
        }

        var index = 0
        var offsetX = 0
        var offsetY = 0
        var offsetZ = 0

        var i = 0
        while (i < transformation.fm.size) {
            val fIndex = transformation.fm[i]
            if (fIndex < def.vertexGroups.size) {
                val vg = def.vertexGroups[fIndex]

                var j = 0
                while (j < vg.size) {
                    val vIndex = vg[j]
                    offsetX += def.vertexPositionsX[vIndex]
                    offsetY += def.vertexPositionsY[vIndex]
                    offsetZ += def.vertexPositionsZ[vIndex]
                    ++index
                    ++j
                }
            }
            ++i
        }

        if (index > 0) {
            offsetX = transformation.dx + offsetX / index
            offsetY = transformation.dy + offsetY / index
            offsetZ = transformation.dz + offsetZ / index
        } else {
            offsetX = transformation.dx
            offsetY = transformation.dy
            offsetZ = transformation.dz
        }

        points.add(ReferencePoint(Vector3f(
                    offsetX.toFloat(),
                    offsetY.toFloat(),
                    offsetZ.toFloat()), 0f, 2f)
        )
    }

    private fun prepare() {
        shader.start()
        glBindVertexArray(quad.vaoId)
        glEnableVertexAttribArray(0)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glDisable(GL_DEPTH_TEST)
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