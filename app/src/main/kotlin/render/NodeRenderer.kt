package render

import animation.ReferenceNode
import animation.TransformationType
import entity.Camera
import model.Model
import shader.NodeShader
import net.runelite.cache.definitions.ModelDefinition
import org.joml.*
import org.liquidengine.legui.event.MouseClickEvent
import org.liquidengine.legui.input.Mouse
import org.lwjgl.opengl.GL30.*
import util.MatrixCreator

class NodeRenderer(private val context: RenderContext) {

    private val quad: Model
    private val loader = Loader()

    private val shader = NodeShader()
    private var viewMatrix = Matrix4f()

    var enabled = false
    val nodes = HashSet<ReferenceNode>()
    var rootNode: ReferenceNode? = null
    var selectedNode: ReferenceNode? = null
    var selectedType = TransformationType.REFERENCE

    init {
        val vertices = floatArrayOf(-0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.5f, -0.5f)
        quad = loader.loadToVao(vertices, 2)
    }

    fun addNode(node: ReferenceNode, def: ModelDefinition) {
        if (!enabled || node.children.size == 0) {
            return
        }

        node.position = node.getPosition(def)
        if (node.position == Vector3f(-0f, 0f, 0f)) { // Ignore origin
            return
        }

        node.highlighted = false
        nodes.add(node)
    }

    fun render(camera: Camera) {
        if (!enabled) {
            return
        }

        context.lineRenderer.renderSkeleton(nodes, rootNode, camera) // Render skeleton behind nodes
        prepare()
        viewMatrix = MatrixCreator.createViewMatrix(camera)
        getClosestNode()?.highlighted = true

        for (node in nodes) {
            shader.setHighlighted(node.highlighted || node.isToggled(selectedNode))
            shader.setRoot(node.id == rootNode?.id)
            loadMatrices(node)
            glDrawArrays(GL_TRIANGLE_STRIP, 0, quad.vertexCount)
        }
        finish()
    }

    private fun prepare() {
        shader.start()
        glBindVertexArray(quad.vaoId)
        glEnableVertexAttribArray(0)
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL)
        glDisable(GL_DEPTH_TEST)
    }

    private fun getClosestNode(): ReferenceNode? {
        val scale = getNodeScale()
        val ray = calculateRay()
        var minDistance = Float.MAX_VALUE
        var closest: ReferenceNode? = null

        for (node in nodes) {
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
        mousePosition.sub(context.framebuffer.position)

        val origin = Vector3f()
        val dir = Vector3f()
        Matrix4f(context.entityRenderer.projectionMatrix)
            .mul(viewMatrix)
            .unprojectRay(mousePosition.x, mousePosition.y, intArrayOf(0, 0,
                context.framebuffer.size.x.toInt(), context.framebuffer.size.y.toInt()), origin, dir
        )
        return Rayf(origin, dir)
    }

    fun handleClick(button: Mouse.MouseButton, action: MouseClickEvent.MouseClickAction) {
        if (enabled && button == Mouse.MouseButton.MOUSE_BUTTON_LEFT &&
            action == MouseClickEvent.MouseClickAction.CLICK) {
            clickNode()
        }
    }

    private fun clickNode() {
        val closest = getClosestNode()?: return
        selectNode(closest)
        context.animationHandler.setPlay(false)
    }

    private fun selectNode(node: ReferenceNode) {
        selectedNode = node
        if (!node.hasType(selectedType)) {
            selectedType = TransformationType.REFERENCE
        }
        context.gizmoRenderer.enable(node, selectedType)
        context.gui.editorPanel.setNode(node, selectedType)
    }

    fun reselectNode() {
        val node = nodes.firstOrNull { it.isToggled(selectedNode) }?: return
        selectNode(node)
    }

    fun reset() {
        selectedNode = null
        nodes.clear()
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
        modelMatrix.scale(getNodeScale())
        shader.loadModelViewMatrix(Matrix4f(viewMatrix).mul(modelMatrix))
        shader.loadProjectionMatrix(context.entityRenderer.projectionMatrix)
    }

    private fun getNodeScale(): Float {
        val base = 2.5f
        return base + context.entity!!.size
    }

    private fun finish() {
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