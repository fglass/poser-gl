package render

import entity.Camera
import entity.ENTITY_POS
import entity.ENTITY_ROT
import model.Model
import org.lwjgl.opengl.GL30.*
import shader.PlaneShader
import util.Maths

class PlaneRenderer(private val framebuffer: Framebuffer) {

    private val quad: Model
    private val loader = Loader()
    private val shader = PlaneShader()

    init {
        val vertices = ArrayList<Float>()

        // Draw grid
        for (i in 0..20) {
            val x = (i - 10) / 4f
            vertices.add(x)
            vertices.add(2.5f)

            vertices.add(x)
            vertices.add(-2.5f)

            vertices.add(2.5f)
            vertices.add(x)

            vertices.add(-2.5f)
            vertices.add(x)
        }
        quad = loader.loadToVao(vertices.toFloatArray())
    }

    fun render(camera: Camera) {
        prepare()
        loadMatrices(camera)
        glDrawArrays(GL_LINES, 0, quad.vertexCount)
        finish()
    }

    private fun prepare() {
        shader.start()
        glBindVertexArray(quad.vaoId)
        glEnableVertexAttribArray(0)
    }

    private fun loadMatrices(camera: Camera) {
        shader.loadTransformationMatrix(Maths.createTransformationMatrix(ENTITY_POS, ENTITY_ROT, 75f))
        shader.loadProjectionMatrix(framebuffer.entityRenderer.projectionMatrix)
        shader.loadViewMatrix(camera)
    }

    private fun finish() {
        glDisableVertexAttribArray(0)
        glBindVertexArray(0)
        shader.stop()
    }

    fun cleanUp() {
        loader.cleanUp()
        shader.cleanUp()
    }
}