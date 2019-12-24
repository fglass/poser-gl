package api.definition

data class NpcDef(val id: Int) {
    var name = "null"
    var models: IntArray? = null
    var models2: IntArray? = null
    var tileSpacesOccupied = 0
    var recolorToFind: ShortArray? = null
    var recolorToReplace: ShortArray? = null
}