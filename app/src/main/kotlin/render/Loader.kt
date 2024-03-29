package render

import api.definition.ModelDefinition
import model.Model
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30
import java.nio.FloatBuffer
import java.nio.IntBuffer

class Loader {

    private val vaos = ArrayList<Int>()
    private val vbos = ArrayList<Int>()

    fun loadToVao(positions: IntArray, normals: IntArray, definition: ModelDefinition): Model {
        val vaoId = createVao()
        storeIntData(0, 4, positions)
        storeIntData(1, 3, normals)
        unbindVao()
        return Model(vaoId, positions.size / 4, definition)
    }

    fun loadToVao(positions: FloatArray, size: Int = 3): Model {
        val vaoId = createVao()
        storeFloatData(0, size, positions)
        unbindVao()
        return Model(vaoId, positions.size / size, ModelDefinition())
    }

    private fun createVao(): Int {
        val vaoId = GL30.glGenVertexArrays()
        vaos.add(vaoId)
        GL30.glBindVertexArray(vaoId)
        return vaoId
    }

    private fun storeFloatData(attributeNumber: Int, size: Int, data: FloatArray) {
        val vboId = GL15.glGenBuffers()
        vbos.add(vboId)
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId)
        val buffer = toFloatBuffer(data)
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW)
        GL20.glVertexAttribPointer(attributeNumber, size, GL11.GL_FLOAT, false, 0, 0)
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0)
    }

    private fun storeIntData(attributeNumber: Int, size: Int, data: IntArray) {
        val vboId = GL15.glGenBuffers()
        vbos.add(vboId)
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId)
        val buffer = toIntBuffer(data)
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW)
        GL30.glVertexAttribIPointer(attributeNumber, size, GL11.GL_INT, 0, 0)
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0)
    }

    private fun toFloatBuffer(data: FloatArray): FloatBuffer {
        val buffer = BufferUtils.createFloatBuffer(data.size)
        buffer.put(data)
        buffer.flip()
        return buffer
    }

    private fun toIntBuffer(data: IntArray): IntBuffer {
        val buffer = BufferUtils.createIntBuffer(data.size)
        buffer.put(data)
        buffer.flip()
        return buffer
    }

    private fun unbindVao() {
        GL30.glBindVertexArray(0)
    }

    fun cleanUp() {
        vaos.forEach { GL30.glDeleteVertexArrays(it) }
        vbos.forEach { GL30.glDeleteBuffers(it) }
        vaos.clear()
        vbos.clear()
    }
}