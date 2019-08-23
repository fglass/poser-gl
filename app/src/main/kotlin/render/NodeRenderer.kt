package render

import animation.ReferenceNode
import animation.TransformationType
import model.Model
import shader.NodeShader
import net.runelite.cache.definitions.ModelDefinition
import org.joml.*
import org.liquidengine.legui.event.MouseClickEvent
import org.liquidengine.legui.input.Mouse
import org.lwjgl.opengl.GL30.*
import util.MouseHandler

class NodeRenderer(private val context: RenderContext, private val mouse: MouseHandler) {

    private val quad: Model
    private val loader = Loader()
    private val shader = NodeShader()

    var enabled = false
    val nodes = HashSet<ReferenceNode>()
    var rootNode: ReferenceNode? = null
    var selectedNode: ReferenceNode? = null
    var selectedType = TransformationType.REFERENCE

    init {
        val vertices = floatArrayOf(-0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.5f, -0.5f)
        quad = loader.loadToVao(vertices, 2)
    }

    fun toggle() {
        enabled = !enabled
        if (!enabled) {
            reset()
            context.gizmoRenderer.reset()
        }
    }

    fun addNode(node: ReferenceNode, def: ModelDefinition) {
        if (!enabled) {
            return
        }

        node.setPosition(def)
        if (node.position != Vector3f(-0f, 0f, 0f)) { // Ignore origin
            node.highlighted = false
            nodes.add(node)
        }
    }

    fun render(viewMatrix: Matrix4f, ray: Rayf) {
        if (!enabled) {
            return
        }

        context.lineRenderer.renderSkeleton(nodes, rootNode, viewMatrix) // Render skeleton behind nodes
        prepare()

        val closest = getClosestNode(ray)
        closest?.let(::handleClosestNode)

        for (node in nodes) {
            // Render selected node later to display on top of gizmo
            if (node.id == selectedNode?.id) {
                selectedNode = node
                continue
            }

            shader.setHighlighted(node.highlighted)
            shader.setRoot(node.id == rootNode?.id)
            loadMatrices(node, viewMatrix)
            glDrawArrays(GL_TRIANGLE_STRIP, 0, quad.vertexCount)
        }
        finish()
    }

    fun renderSelected(viewMatrix: Matrix4f) {
        if (!enabled) {
            return
        }

        selectedNode?.let {
            prepare()
            shader.setHighlighted(true)
            loadMatrices(it, viewMatrix)
            glDrawArrays(GL_TRIANGLE_STRIP, 0, quad.vertexCount)
            finish()
        }
    }

    private fun prepare() {
        shader.start()
        glBindVertexArray(quad.vaoId)
        glEnableVertexAttribArray(0)
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL)
        glDisable(GL_DEPTH_TEST)
    }

    private fun getClosestNode(ray: Rayf): ReferenceNode? {
        val scale = getNodeScale()
        var minDistance = Float.MAX_VALUE
        var closest: ReferenceNode? = null

        for (node in nodes) {
            val min = Vector3f(node.position).sub(Vector3f(scale))
            val max = Vector3f(node.position).add(Vector3f(scale))
            val nearFar = Vector2f()

            if (Intersectionf.intersectRayAab(ray, AABBf(min, max), nearFar) && nearFar.x < minDistance) {
                minDistance = nearFar.x
                closest = node
            }
        }
        return closest
    }

    private fun handleClosestNode(node: ReferenceNode) {
        if (mouse.clicked) {
            selectNode(node)
            context.animationHandler.setPlay(false)
        }
        node.highlighted = true
    }

    private fun selectNode(node: ReferenceNode) { // TODO: unselecting on reclick
        selectedNode = node
        if (!node.hasType(selectedType)) {
            selectedType = TransformationType.REFERENCE
        }
        context.gizmoRenderer.enable(node, selectedType)
        context.gui.editorPanel.setNode(node, selectedType)
    }

    fun reselectNode() {
        val node = nodes.firstOrNull { it.id == selectedNode?.id }?: return
        selectNode(node)
    }

    fun updateType(type: TransformationType) {
        selectedType = type
        selectedNode?.let {
            context.gizmoRenderer.enable(it, selectedType)
        }
    }

    fun reset() {
        selectedNode = null
        nodes.clear()
    }

    private fun loadMatrices(node: ReferenceNode, viewMatrix: Matrix4f) {
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