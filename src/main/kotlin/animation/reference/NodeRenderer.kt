package animation.reference

import animation.AnimationHandler
import entity.Camera
import model.Model
import Processor
import net.runelite.cache.definitions.ModelDefinition
import org.joml.*
import org.liquidengine.legui.event.MouseClickEvent
import org.liquidengine.legui.input.Mouse
import org.lwjgl.opengl.GL30.*
import render.Loader
import utils.Maths

class NodeRenderer(private val context: Processor, private var projectionMatrix: Matrix4f,
                   private var fboSize: Vector2f, private var fboPosition: Vector2f) {

    private val quad: Model
    private val loader = Loader()
    private val shader = NodeShader()
    private var viewMatrix = Matrix4f()
    private val nodes = HashSet<ReferenceNode>()
    var enabled = false
    var activeType = 0
    var selected: Int? = null

    init {
        val vertices = floatArrayOf(-0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.5f, -0.5f)
        quad = loader.loadToVao(vertices)
    }

    fun addNode(def: ModelDefinition, tf: AnimationHandler.Transformation) {
        if (tf.type != activeType) {
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
        nodes.add(ReferenceNode(tf, offset))
    }

    fun reset() {
        nodes.clear()
    }

    fun render(camera: Camera) {
        if (!enabled) {
            return
        }

        prepare()
        viewMatrix = Maths.createViewMatrix(camera)
        getClosestNode()?.highlighted = true

        for (node in nodes) {
            shader.setHighlighted(node.highlighted || node.transformation.id == selected)
            loadMatrices(node)
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

    private fun getClosestNode(): ReferenceNode? {
        val ray = calculateRay()
        var minDistance = Float.MAX_VALUE
        var closest: ReferenceNode? = null

        for (node in nodes) {
            val scale = node.scale
            val min = Vector3f(node.position).sub(scale, scale, scale)
            val max = Vector3f(node.position).add(scale, scale, scale)
            val nearFar = Vector2f()

            if (Intersectionf.intersectRayAab(ray, AABBf(min, max), nearFar) && nearFar.x < minDistance) {
                minDistance = nearFar.x
                closest = node
            }
        }
        return closest
    }

    private fun calculateRay(): Rayf {
        val mousePosition = Mouse.getCursorPosition()
        mousePosition.sub(fboPosition)

        val origin = Vector3f()
        val dir = Vector3f()
        Matrix4f(projectionMatrix).mul(viewMatrix).unprojectRay(
            mousePosition.x, mousePosition.y, intArrayOf(0, 0, fboSize.x.toInt(), fboSize.y.toInt()), origin, dir
        )
        return Rayf(origin, dir)
    }

    fun handleClick(button: Mouse.MouseButton, action: MouseClickEvent.MouseClickAction) {
        if (button != Mouse.MouseButton.MOUSE_BUTTON_LEFT || action != MouseClickEvent.MouseClickAction.CLICK ||
            !enabled) {
            return
        }

        val closest = getClosestNode()
        if (closest != null) {
            selected = closest.transformation.id
            context.gui.editorPanel.setNode(closest.transformation)
        }
    }

    private fun loadMatrices(node: ReferenceNode) {
        val modelMatrix = Matrix4f()
        modelMatrix.translate(node.position)
        modelMatrix.m00(viewMatrix.m00())
        modelMatrix.m01(viewMatrix.m10())
        modelMatrix.m02(viewMatrix.m20())
        modelMatrix.m10(viewMatrix.m01())
        modelMatrix.m11(viewMatrix.m11())
        modelMatrix.m12(viewMatrix.m21())
        modelMatrix.m20(viewMatrix.m02())
        modelMatrix.m21(viewMatrix.m12())
        modelMatrix.m22(viewMatrix.m22())
        modelMatrix.scale(node.scale)
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