package api.definition

import net.runelite.cache.definitions.FramemapDefinition

data class FrameDef(val id: Int) {
    lateinit var framemap: FramemapDefinition // TODO: rename
    var translatorCount = 0
    lateinit var indexFrameIds: IntArray
    lateinit var translator_x: IntArray
    lateinit var translator_y: IntArray
    lateinit var translator_z: IntArray
}