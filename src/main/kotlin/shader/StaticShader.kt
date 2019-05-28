package shader

import org.joml.Matrix4f
import utils.Maths
import entity.Camera
import entity.Light

private const val VERTEX_FILE = "src/main/kotlin/shader/vs.glsl"
private const val FRAGMENT_FILE = "src/main/kotlin/shader/fs.glsl"

class StaticShader: ShaderProgram(VERTEX_FILE, FRAGMENT_FILE) {

    private var locationTransformationMatrix = 0
    private var locationProjectionMatrix = 0
    private var locationViewMatrix = 0
    private var locationLightPosition = 0
    private var locationLightColour = 0
    private var locationUseShading = 0

    override fun getAllUniformLocations() {
        locationTransformationMatrix = getUniformLocation("transformationMatrix")
        locationProjectionMatrix = getUniformLocation("projectionMatrix")
        locationViewMatrix = getUniformLocation("viewMatrix")
        locationLightPosition = getUniformLocation("lightPosition")
        locationLightColour = getUniformLocation("lightColour")
        locationUseShading = getUniformLocation("useShading")
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

    fun loadLight(light: Light) {
        loadVector(locationLightPosition, light.position)
        loadVector(locationLightColour, light.colour)
    }

    fun loadShadingToggle(use: Boolean) {
        loadBoolean(locationUseShading, use)
    }

    override fun bindAttributes() {
        bindAttribute(0, "position")
        bindAttribute(1, "normal")
    }
}
