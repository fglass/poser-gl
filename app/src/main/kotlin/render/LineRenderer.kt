package render

import animation.ReferenceNode
import entity.Camera
import entity.ENTITY_POS
import entity.ENTITY_ROT
import entity.ENTITY_SCALE
import model.Model
import shader.LineShader
import org.lwjgl.opengl.GL30.*
import util.Maths

private const val VERTEX_FILE = "shader/line-vs.glsl"
private const val FRAGMENT_FILE = "shader/line-fs.glsl"

class LineRenderer(private val framebuffer: Framebuffer) {

    private val grid: Model
    private val gridLoader = Loader()
    private val skeletonLoader = Loader()
    private val shader = LineShader(VERTEX_FILE, FRAGMENT_FILE)

    init {
        grid = getGrid()
    }

    private fun getGrid(): Model {
        val vertices = ArrayList<Float>()
        val offset = 2.5f

        for (i in 0..20 step 2) {
            val x = (i - 10) / 4f
            vertices.add(x)
            vertices.add(0f)
            vertices.add(offset)

            vertices.add(x)
            vertices.add(0f)
            vertices.add(-offset)

            vertices.add(offset)
            vertices.add(0f)
            vertices.add(x)

            vertices.add(-offset)
            vertices.add(0f)
            vertices.add(x)
        }
        return gridLoader.loadToVao(vertices.toFloatArray())
    }

    fun renderGrid(camera: Camera) {
        prepare(grid, false)
        loadMatrices(camera, 75f)
        glDrawArrays(GL_LINES, 0, grid.vertexCount)
        finish()
    }

    fun renderSkeleton(nodes: Set<ReferenceNode>, camera: Camera) {
        skeletonLoader.cleanUp()
        for (node in nodes) {
            val parent = node.parent?: continue
            val vertices = floatArrayOf(
                node.position.x, node.position.y, node.position.z,
                parent.position.x, parent.position.y, parent.position.z
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
        shader.loadTransformationMatrix(Maths.createTransformationMatrix(ENTITY_POS, ENTITY_ROT, scale))
        shader.loadProjectionMatrix(framebuffer.entityRenderer.projectionMatrix)
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