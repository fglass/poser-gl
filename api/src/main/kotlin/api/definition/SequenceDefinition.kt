package api.definition

data class SequenceDefinition(val id: Int) {
    lateinit var frameIds: IntArray
    lateinit var frameLengths: IntArray
    var leftHandItem = -1
    var rightHandItem = -1
    var loopType = 2

    fun isValid() = ::frameIds.isInitialized && ::frameLengths.isInitialized
}