package api.definition

data class FrameMapDefinition(var id: Int = -1) {
    var length = 0
    lateinit var types: IntArray
    lateinit var maps: Array<IntArray>
}