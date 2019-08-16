package shader

import org.joml.Matrix4f
import org.joml.Vector4f

private const val VERTEX_FILE = "shader/gizmo-vs.glsl"
private const val FRAGMENT_FILE = "shader/gizmo-fs.glsl"

class GizmoShader: ShaderProgram(VERTEX_FILE, FRAGMENT_FILE) {

    private var locationTransformationMatrix = 0
    private var locationProjectionMatrix = 0
    private var locationViewMatrix = 0
    private var locationColour = 0

    override fun getAllUniformLocations() {
        locationTransformationMatrix = getUniformLocation("transformationMatrix")
        locationProjectionMatrix = getUniformLocation("projectionMatrix")
        locationViewMatrix = getUniformLocation("viewMatrix")
        locationColour = getUniformLocation("colour")
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

    fun loadColour(colour: Vector4f) {
        loadVector(locationColour, colour)
    }

    override fun bindAttributes() {
        bindAttribute(0, "position")
    }
}