package api.definition

data class FrameDefinition(val id: Int) {
    var length = 0
    lateinit var frameMap: FrameMapDefinition
    lateinit var indices: IntArray
    lateinit var deltaX: IntArray
    lateinit var deltaY: IntArray
    lateinit var deltaZ: IntArray
}