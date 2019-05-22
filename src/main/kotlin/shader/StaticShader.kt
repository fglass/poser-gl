package shader

import org.joml.Matrix4f
import utils.Maths
import entity.Camera

private const val VERTEX_FILE = "src/main/kotlin/shader/vs.glsl"
private const val FRAGMENT_FILE = "src/main/kotlin/shader/fs.glsl"

class StaticShader: ShaderProgram(VERTEX_FILE, FRAGMENT_FILE) {

    private var locationTransformationMatrix = 0
    private var locationProjectionMatrix = 0
    private var locationViewMatrix = 0

    override fun getAllUniformLocations() {
        locationTransformationMatrix = getUniformLocation("transformationMatrix")
        locationProjectionMatrix = getUniformLocation("projectionMatrix")
        locationViewMatrix = getUniformLocation("viewMatrix")
    }

    fun loadTransformationMatrix(matrix: Matrix4f) {
        loadMatrix(locationTransformationMatrix, matrix)
    }

    fun loadProjectionMatrix(matrix: Matrix4f) {
        loadMatrix(locationProjectionMatrix, matrix)
    }

    fun loadViewMatrix(camera: Camera) {
        val matrix = Maths.createViewMatrix(camera)
        loadMatrix(locationViewMatrix, matrix)
    }

    override fun bindAttributes() {
        bindAttribute(0, "position")
    }
}
