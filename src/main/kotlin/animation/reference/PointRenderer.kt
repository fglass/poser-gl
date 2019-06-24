package animation.reference

import animation.AnimationHandler
import entity.Camera
import model.Model
import net.runelite.cache.definitions.ModelDefinition
import org.joml.*
import org.liquidengine.legui.input.Mouse
import org.lwjgl.opengl.GL30.*
import render.Loader
import utils.Maths
import java.lang.Math

class PointRenderer(private val projectionMatrix: Matrix4f, private val fboSize: Vector2f) {

    private val quad: Model
    private val loader = Loader()
    private val shader = ReferenceShader()
    private val points = ArrayList<ReferencePoint>()
    var enabled = false

    init {
        val vertices = floatArrayOf(-0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.5f, -0.5f)
        quad = loader.loadToVao(vertices)
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
        points.add(ReferencePoint(offset, 0f, 2.5f))
    }

    fun reset() {
        points.clear()
    }

    fun render(camera: Camera) {
        if (!enabled) {
            return
        }

        prepare()
        val viewMatrix = Maths.createViewMatrix(camera)
        val ray = calculateRay(viewMatrix)

        for (point in points) {
            val scale = point.scale
            val min = Vector3f(point.position).sub(scale, scale, scale)
            val max = Vector3f(point.position).add(scale, scale, scale)
            val intersection = Intersectionf.intersectRayAab(ray, AABBf(min, max), Vector2f())
            shader.setHighlighted(intersection) // TODO closest only, toggling

            loadMatrices(point.position, point.rotation, scale, viewMatrix)
            glDrawArrays(GL_TRIANGLE_STRIP, 0, quad.vertexCount) // TODO instanced rendering
        }

        finish()
    }

    private fun prepare() {
        shader.start()
        glBindVertexArray(quad.vaoId)
        glEnableVertexAttribArray(0)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glDisable(GL_DEPTH_TEST)
    }

    private fun calculateRay(viewMatrix: Matrix4f): Rayf {
        val mousePosition = Mouse.getCursorPosition()
        val fboPosition = Vector2f(174f, 5f) // TODO pos and size
        mousePosition.sub(fboPosition)

        val w = (fboSize.x / 2).toInt()
        val h = (fboSize.y / 2).toInt()
        val origin = Vector3f()
        val dir = Vector3f()
        Matrix4f(projectionMatrix).mul(viewMatrix)
                                  .unprojectRay(mousePosition.x, mousePosition.y, intArrayOf(0, 0, w, h), origin, dir)
        return Rayf(origin, dir)
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
        shader.loadModelViewMatrix(Matrix4f(viewMatrix).mul(modelMatrix))
        shader.loadProjectionMatrix(projectionMatrix)
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