package api.definition

data class FrameMapDef(var id: Int = -1) {
    lateinit var types: IntArray
    lateinit var frameMaps: Array<IntArray>
    var length = 0
}