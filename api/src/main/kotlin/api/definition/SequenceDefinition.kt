package api.definition

data class SequenceDefinition(val id: Int) {

    lateinit var frameIds: IntArray
    lateinit var frameLengths: IntArray
    var loopOffset = -1
    var leftHandItem = -1
    var rightHandItem = -1

    fun isValid() = ::frameIds.isInitialized && ::frameLengths.isInitialized
}