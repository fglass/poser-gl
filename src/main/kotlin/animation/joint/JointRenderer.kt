package animation.joint

import animation.AnimationHandler
import entity.Camera
import input.MouseHandler
import model.Model
import net.runelite.cache.definitions.ModelDefinition
import org.joml.*
import org.liquidengine.legui.input.Mouse
import org.lwjgl.opengl.GL30.*
import render.Loader
import utils.Maths

class JointRenderer(private var projectionMatrix: Matrix4f, private var fboSize: Vector2f,
                    private var fboPosition: Vector2f) {

    private val quad: Model
    private val loader = Loader()
    private val shader = JointShader()
    private val joints = HashSet<Joint>()
    private var selected: Int? = null
    private var canSelect = true
    var enabled = false

    init {
        val vertices = floatArrayOf(-0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.5f, -0.5f)
        quad = loader.loadToVao(vertices)
    }

    fun addJoint(def: ModelDefinition, tf: AnimationHandler.Transformation) {
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
        joints.add(Joint(joints.size, offset))
    }

    fun reset() {
        joints.clear()
    }

    fun render(camera: Camera) {
        if (!enabled) {
            return
        }

        prepare()
        val viewMatrix = Maths.createViewMatrix(camera)
        highlight(viewMatrix, camera.mouse)

        for (joint in joints) {
            shader.setHighlighted(joint.highlighted || joint.id == selected)
            loadMatrices(joint, viewMatrix)
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
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL)
        glDisable(GL_DEPTH_TEST)
    }

    private fun highlight(viewMatrix: Matrix4f, mouse: MouseHandler) {
        val ray = calculateRay(viewMatrix)
        var minDistance = Float.MAX_VALUE
        var closest: Joint? = null

        for (joint in joints) {
            val scale = joint.scale
            val min = Vector3f(joint.position).sub(scale, scale, scale)
            val max = Vector3f(joint.position).add(scale, scale, scale)
            val nearFar = Vector2f()

            if (Intersectionf.intersectRayAab(ray, AABBf(min, max), nearFar) && nearFar.x < minDistance) {
                minDistance = nearFar.x
                closest = joint
            }
        }

        if (closest == null) {
            return
        }

        if (mouse.pressed && canSelect) {
            selected = closest.id
            canSelect = false
        } else if (!mouse.pressed){
            closest.highlighted = true
            canSelect = true
        }
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

    private fun loadMatrices(joint: Joint, viewMatrix: Matrix4f) {
        val modelMatrix = Matrix4f()
        modelMatrix.translate(joint.position)
        modelMatrix.m00(viewMatrix.m00())
        modelMatrix.m01(viewMatrix.m10())
        modelMatrix.m02(viewMatrix.m20())
        modelMatrix.m10(viewMatrix.m01())
        modelMatrix.m11(viewMatrix.m11())
        modelMatrix.m12(viewMatrix.m21())
        modelMatrix.m20(viewMatrix.m02())
        modelMatrix.m21(viewMatrix.m12())
        modelMatrix.m22(viewMatrix.m22())
        modelMatrix.scale(joint.scale)
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