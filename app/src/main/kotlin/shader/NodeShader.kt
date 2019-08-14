package shader

import org.joml.Matrix4f

private const val VERTEX_FILE =  "shader/node-vs.glsl"
private const val FRAGMENT_FILE = "shader/node-fs.glsl"

class NodeShader: ShaderProgram(VERTEX_FILE, FRAGMENT_FILE) {

    private var locationProjectionMatrix = 0
    private var locationModelViewMatrix = 0
    private var locationHighlight = 0
    private var locationRoot = 0

    override fun getAllUniformLocations() {
        locationProjectionMatrix = getUniformLocation("projectionMatrix")
        locationModelViewMatrix = getUniformLocation("modelViewMatrix")
        locationHighlight = getUniformLocation("isHighlighted")
        locationRoot = getUniformLocation("isRoot")
    }

	fun loadProjectionMatrix(matrix: Matrix4f ) {
		loadMatrix(locationProjectionMatrix, matrix)
	}

    fun loadModelViewMatrix(matrix: Matrix4f) {
        loadMatrix(locationModelViewMatrix, matrix)
    }

    fun setHighlighted(use: Boolean) {
        loadBoolean(locationHighlight, use)
    }

    fun setRoot(use: Boolean) {
        loadBoolean(locationRoot, use)
    }

    override fun bindAttributes() {
        bindAttribute(0, "position")
    }
}