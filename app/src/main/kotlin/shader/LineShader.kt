package shader

import entity.Camera
import org.joml.Matrix4f
import util.MatrixCreator

private const val VERTEX_FILE = "shader/line-vs.glsl"
private const val FRAGMENT_FILE = "shader/line-fs.glsl"

class LineShader: ShaderProgram(VERTEX_FILE, FRAGMENT_FILE) {

    private var locationTransformationMatrix = 0
    private var locationProjectionMatrix = 0
    private var locationViewMatrix = 0
    private var locationIsGrid = 0

    override fun getAllUniformLocations() {
        locationTransformationMatrix = getUniformLocation("transformationMatrix")
        locationProjectionMatrix = getUniformLocation("projectionMatrix")
        locationViewMatrix = getUniformLocation("viewMatrix")
        locationIsGrid = getUniformLocation("isGrid")
    }

    fun loadTransformationMatrix(matrix: Matrix4f) {
        loadMatrix(locationTransformationMatrix, matrix)
    }

    fun loadProjectionMatrix(matrix: Matrix4f) {
        loadMatrix(locationProjectionMatrix, matrix)
    }

    fun loadViewMatrix(matrix: Matrix4f) {
        loadMatrix(locationViewMatrix, matrix)
    }

    fun loadGridToggle(isGrid: Boolean) {
        loadBoolean(locationIsGrid, isGrid)
    }

    override fun bindAttributes() {
        bindAttribute(0, "position")
    }
}