package animation.reference

import shader.ShaderProgram
import org.joml.Matrix4f

private const val VERTEX_FILE = "src/main/kotlin/animation/reference/node-vs.glsl"
private const val FRAGMENT_FILE = "src/main/kotlin/animation/reference/node-fs.glsl"

class NodeShader: ShaderProgram(VERTEX_FILE, FRAGMENT_FILE) {

    private var locationProjectionMatrix = 0
    private var locationModelViewMatrix = 0
    private var locationHighlight = 0

    override fun getAllUniformLocations() {
        locationProjectionMatrix = super.getUniformLocation("projectionMatrix")
        locationModelViewMatrix = super.getUniformLocation("modelViewMatrix")
        locationHighlight = getUniformLocation("isHighlighted")
    }

	fun loadProjectionMatrix(matrix: Matrix4f ) {
		super.loadMatrix(locationProjectionMatrix, matrix)
	}

    fun loadModelViewMatrix(matrix: Matrix4f) {
        super.loadMatrix(locationModelViewMatrix, matrix)
    }

    fun setHighlighted(use: Boolean) {
        loadBoolean(locationHighlight, use)
    }

    override fun bindAttributes() {
        super.bindAttribute(0, "position")
    }
}