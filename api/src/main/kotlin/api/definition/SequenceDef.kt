package api.definition

data class SequenceDef(val id: Int) {
    lateinit var frameIDs: IntArray
    lateinit var frameLenghts: IntArray // TODO: rename
    var leftHandItem = -1
    var rightHandItem = -1

    fun isValid() = ::frameIDs.isInitialized && ::frameLenghts.isInitialized
}