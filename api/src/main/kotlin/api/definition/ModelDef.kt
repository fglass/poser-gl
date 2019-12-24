package api.definition

data class ModelDef(val id: Int = -1) {

    companion object {
        var animOffsetX = 0
        var animOffsetY = 0
        var animOffsetZ = 0
    }

    var vertexCount = 0
    var faceCount = 0
    var priority = 0

    lateinit var vertexPositionsX: IntArray
    lateinit var vertexPositionsY: IntArray
    lateinit var vertexPositionsZ: IntArray
    lateinit var faceVertexIndices1: IntArray
    lateinit var faceVertexIndices2: IntArray
    lateinit var faceVertexIndices3: IntArray

    lateinit var vertexGroups: Array<IntArray>
    var vertexSkins: IntArray? = null
    var origVX: IntArray? = null
    var origVY: IntArray? = null
    var origVZ: IntArray? = null

    lateinit var faceColors: ShortArray
    var faceAlphas: ByteArray? = null
    var faceRenderPriorities: ByteArray? = null
    var faceRenderTypes: ByteArray? = null
}