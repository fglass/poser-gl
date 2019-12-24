package api.definition

data class NpcDefinition(val id: Int) {
    var name = "null"
    var models: IntArray? = null
    var models2: IntArray? = null
    var spaces = 0
    var originalColours: ShortArray? = null
    var newColours: ShortArray? = null
}