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

class PointRenderer(private var projectionMatrix: Matrix4f, private var fboSize: Vector2f,
                    private var fboPosition: Vector2f) {

    private val quad: Model
    private val loader = Loader()
    private val shader = ReferenceShader()
    private val points = HashSet<ReferencePoint>()
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
        points.add(ReferencePoint(offset))
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
        setHighlighted(viewMatrix)

        for (point in points) {
            shader.setHighlighted(point.highlighted) // TODO toggling
            loadMatrices(point, viewMatrix)
            glDrawArrays(GL_TRIANGLE_STRIP, 0, quad.vertexCount)
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

    private fun setHighlighted(viewMatrix: Matrix4f) {
        val ray = calculateRay(viewMatrix)
        var minDistance = Float.MAX_VALUE
        var closest: ReferencePoint? = null

        for (point in points) {
            val scale = point.scale
            val min = Vector3f(point.position).sub(scale, scale, scale)
            val max = Vector3f(point.position).add(scale, scale, scale)
            val nearFar = Vector2f()

            if (Intersectionf.intersectRayAab(ray, AABBf(min, max), nearFar) && nearFar.x < minDistance) {
                minDistance = nearFar.x
                closest = point
            }
            point.highlighted = false // Reset
        }
        closest?.highlighted = true
    }

    private fun calculateRay(viewMatrix: Matrix4f): Rayf {
        val mousePosition = Mouse.getCursorPosition()
        mousePosition.sub(fboPosition)

        val origin = Vector3f()
        val dir = Vector3f()
        Matrix4f(projectionMatrix).mul(viewMatrix).unprojectRay(
            mousePosition.x, mousePosition.y, intArrayOf(0, 0, fboSize.x.toInt(), fboSize.y.toInt()), origin, dir
        )
        return Rayf(origin, dir)
    }

    private fun loadMatrices(point: ReferencePoint, viewMatrix: Matrix4f) {
        val modelMatrix = Matrix4f()
        modelMatrix.translate(point.position)
        modelMatrix.m00(viewMatrix.m00())
        modelMatrix.m01(viewMatrix.m10())
        modelMatrix.m02(viewMatrix.m20())
        modelMatrix.m10(viewMatrix.m01())
        modelMatrix.m11(viewMatrix.m11())
        modelMatrix.m12(viewMatrix.m21())
        modelMatrix.m20(viewMatrix.m02())
        modelMatrix.m21(viewMatrix.m12())
        modelMatrix.m22(viewMatrix.m22())
        modelMatrix.scale(point.scale)
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

    fun resize(projectionMatrix: Matrix4f, fboSize: Vector2f, fboPosition: Vector2f) {
        this.projectionMatrix = projectionMatrix
        this.fboSize = fboSize
        this.fboPosition = fboPosition
    }

    fun cleanUp() {
        loader.cleanUp()
        shader.cleanUp()
    }
}