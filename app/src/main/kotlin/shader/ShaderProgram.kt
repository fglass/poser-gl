package shader

import com.spinyowl.legui.util.IOUtil
import mu.KotlinLogging
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger {}

abstract class ShaderProgram(vertexFile: String, fragmentFile: String) {

    private var programId = 0
    private var vertexShaderId = 0
    private var fragmentShaderId = 0
    private val matrixBuffer = BufferUtils.createFloatBuffer(16)

    init {
        vertexShaderId = loadShader(vertexFile, GL20.GL_VERTEX_SHADER)
        fragmentShaderId = loadShader(fragmentFile, GL20.GL_FRAGMENT_SHADER)
        programId = GL20.glCreateProgram()
        GL20.glAttachShader(programId, vertexShaderId)
        GL20.glAttachShader(programId, fragmentShaderId)
        this.bindAttributes()
        GL20.glLinkProgram(programId)
        GL20.glValidateProgram(programId)
        this.getAllUniformLocations()
    }

    fun start() {
        GL20.glUseProgram(programId)
    }

    fun stop() {
        GL20.glUseProgram(0)
    }

    fun cleanUp() {
        stop()
        GL20.glDetachShader(programId, vertexShaderId)
        GL20.glDetachShader(programId, fragmentShaderId)
        GL20.glDeleteShader(vertexShaderId)
        GL20.glDeleteShader(fragmentShaderId)
        GL20.glDeleteProgram(programId)
    }

    fun getUniformLocation(name: String): Int {
        return GL20.glGetUniformLocation(programId, name)
    }

    abstract fun getAllUniformLocations()

    fun bindAttribute(attribute: Int, name: String) {
        GL20.glBindAttribLocation(programId, attribute, name)
    }

    abstract fun bindAttributes()

    fun loadVector(location: Int, vector: Vector3f) {
        GL20.glUniform3f(location, vector.x, vector.y, vector.z)
    }

    fun loadVector(location: Int, vector: Vector4f) {
        GL20.glUniform4f(location, vector.x, vector.y, vector.z, vector.w)
    }

    fun loadBoolean(location: Int, value: Boolean) {
        GL20.glUniform1f(location, if (value) 1f else 0f)
    }

    fun loadMatrix(location: Int, matrix: Matrix4f) {
        GL20.glUniformMatrix4fv(location, false, matrix.get(matrixBuffer))
    }

    private fun loadShader(name: String, type: Int): Int {
        val shaderId = GL20.glCreateShader(type)
        GL20.glShaderSource(shaderId, IOUtil.resourceToString(name))
        GL20.glCompileShader(shaderId)

        if (GL20.glGetShaderi(shaderId, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            logger.error { GL20.glGetShaderInfoLog(shaderId, 500) }
            exitProcess(-1)
        }
        return shaderId
    }
}
