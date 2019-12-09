package render

import animation.ReferenceNode
import animation.TransformationType
import entity.Camera
import entity.ENTITY_POS
import entity.ENTITY_ROT
import entity.ENTITY_SCALE
import model.Model
import org.joml.Matrix4f
import org.lwjgl.opengl.GL11
import shader.LineShader
import org.lwjgl.opengl.GL30.*
import util.MatrixCreator
import kotlin.math.ceil
import kotlin.math.roundToInt

class LineRenderer(private val context: RenderContext) {

    private var grid: Model? = null
    private val gridLoader = Loader()
    private val skeletonLoader = Loader()
    private val shader = LineShader()

    fun setGrid(entitySize: Int) {
        gridLoader.cleanUp()
        val base = if (entitySize > 1) 10f else 9f
        val dimension = ceil((base + entitySize) / 2f) * 2 // Round up to even number
        val offset = dimension / 4f
        val vertices = ArrayList<Float>()

        for (i in 0..(dimension * 2).roundToInt() step 2) {
            val x = (i - dimension) / 4f
            vertices.addVertex(x, 0f, offset)
            vertices.addVertex(x, 0f, -offset)
            vertices.addVertex(offset, 0f, x)
            vertices.addVertex(-offset, 0f, x)
        }
        grid = gridLoader.loadToVao(vertices.toFloatArray())
    }

    private fun ArrayList<Float>.addVertex(x: Float, y: Float, z: Float) {
        add(x)
        add(y)
        add(z)
    }

    fun renderGrid(viewMatrix: Matrix4f) {
        if (context.settingsManager.gridActive) {
            grid?.let {
                prepare(it, false)
                loadMatrices(viewMatrix, 75f)
                glDrawArrays(GL_LINES, 0, it.vertexCount)
                finish()
            }
        }
    }

    fun renderSkeleton(nodes: Set<ReferenceNode>, root: ReferenceNode?, viewMatrix: Matrix4f) {
        skeletonLoader.cleanUp()

        if (!context.settingsManager.jointsActive) {
            return
        }

        for (node in nodes) {
            val parent = node.parent?: continue
            node.parent = nodes.firstOrNull { it.id == parent.id } ?: continue // Update parent reference

            if (node.id == root?.id || parent.id == root?.id || !parent.hasType(TransformationType.ROTATION)) {
                continue
            }

            val vertices = floatArrayOf(
                node.position.x, node.position.y, node.position.z, // Start
                parent.position.x, parent.position.y, parent.position.z // End
            )
            val line = skeletonLoader.loadToVao(vertices)

            prepare(line)
            loadMatrices(viewMatrix, ENTITY_SCALE)
            glDrawArrays(GL_LINES, 0, line.vertexCount)
            finish()
        }
    }

    private fun prepare(line: Model, depth: Boolean = true) {
        shader.start()
        shader.loadGridToggle(line == grid)
        glBindVertexArray(line.vaoId)
        glEnableVertexAttribArray(0)
        glEnable(GL_LINE_SMOOTH)
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST)
        if (depth) {
            glDisable(GL_DEPTH_TEST)
        }
    }

    private fun loadMatrices(viewMatrix: Matrix4f, scale: Float) {
        shader.loadTransformationMatrix(MatrixCreator.createTransformationMatrix(ENTITY_POS, ENTITY_ROT, scale))
        shader.loadProjectionMatrix(context.projectionMatrix)
        shader.loadViewMatrix(viewMatrix)
    }

    private fun finish() {
        glEnable(GL_DEPTH_TEST)
        glDisableVertexAttribArray(0)
        glBindVertexArray(0)
        shader.stop()
    }

    fun cleanUp() {
        gridLoader.cleanUp()
        skeletonLoader.cleanUp()
        shader.cleanUp()
    }
}