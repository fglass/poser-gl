package shader

import entity.Camera
import org.joml.Matrix4f
import util.MatrixCreator

private const val VERTEX_FILE = "shader/gizmo-vs.glsl"
private const val FRAGMENT_FILE = "shader/gizmo-fs.glsl"

class GizmoShader: ShaderProgram(VERTEX_FILE, FRAGMENT_FILE) {

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
        val matrix = MatrixCreator.createViewMatrix(camera)
        loadMatrix(locationViewMatrix, matrix)
    }

    override fun bindAttributes() {
        bindAttribute(0, "position")
    }
}