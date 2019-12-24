package api.definition

data class ItemDefinition(val id: Int) {
    var name = "null"
    var model0 = -1
    var model1 = -1
    var model2 = -1
    var originalColours: ShortArray? = null
    var newColours: ShortArray? = null
}