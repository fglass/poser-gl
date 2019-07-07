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

    private val loader = Loader()
    private val shader = LineShader(VERTEX_FILE, FRAGMENT_FILE)

    fun render(nodes: List<ReferenceNode>, camera: Camera) {
        loader.cleanUp()
        for (node in nodes) {
            val parent = node.parentNode?: continue

            val vertices = floatArrayOf(
                node.position.x, node.position.y, node.position.z,
                parent.position.x, parent.position.y, parent.position.z
            )
            val line = loader.loadToVao(vertices, 3)

            prepare(line)
            loadMatrices(camera)
            glDrawArrays(GL_LINES, 0, line.vertexCount)
            finish()
        }
    }

    private fun prepare(line: Model) {
        shader.start()
        glBindVertexArray(line.vaoId)
        glEnableVertexAttribArray(0)
        glEnable(GL_LINE_SMOOTH)
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST)
        glDisable(GL_DEPTH_TEST)
    }

    private fun loadMatrices(camera: Camera) {
        shader.loadTransformationMatrix(Maths.createTransformationMatrix(ENTITY_POS, ENTITY_ROT, ENTITY_SCALE))
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
        loader.cleanUp()
        shader.cleanUp()
    }
}