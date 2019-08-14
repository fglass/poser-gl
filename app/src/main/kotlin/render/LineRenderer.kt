package render

import animation.ReferenceNode
import animation.TransformationType
import entity.Camera
import entity.ENTITY_POS
import entity.ENTITY_ROT
import entity.ENTITY_SCALE
import model.Model
import shader.LineShader
import org.lwjgl.opengl.GL30.*
import util.MatrixCreator
import kotlin.math.ceil
import kotlin.math.roundToInt

private const val VERTEX_FILE = "shader/line-vs.glsl"
private const val FRAGMENT_FILE = "shader/line-fs.glsl"

class LineRenderer(private val context: RenderContext) {

    private var grid: Model? = null
    private val gridLoader = Loader()
    private val skeletonLoader = Loader()
    private val shader = LineShader(VERTEX_FILE, FRAGMENT_FILE)

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

    fun renderGrid(camera: Camera) {
        if (grid != null) {
            prepare(grid!!, false)
            loadMatrices(camera, 75f)
            glDrawArrays(GL_LINES, 0, grid!!.vertexCount)
            finish()
        }
    }

    fun renderSkeleton(nodes: Set<ReferenceNode>, root: ReferenceNode?, camera: Camera) {
        skeletonLoader.cleanUp()
        for (node in nodes) {
            val parent = node.parent?: continue
            if (node.id == root?.id || parent.id == root?.id || !node.hasRotation() || !parent.hasRotation()) {
                continue
            }

            val vertices = floatArrayOf(
                node.position.x, node.position.y, node.position.z, // Start
                parent.position.x, parent.position.y, parent.position.z // End
            )
            val line = skeletonLoader.loadToVao(vertices)

            prepare(line)
            loadMatrices(camera, ENTITY_SCALE)
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

    private fun loadMatrices(camera: Camera, scale: Float) {
        shader.loadTransformationMatrix(MatrixCreator.createTransformationMatrix(ENTITY_POS, ENTITY_ROT, scale))
        shader.loadProjectionMatrix(context.entityRenderer.projectionMatrix)
        shader.loadViewMatrix(camera)
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