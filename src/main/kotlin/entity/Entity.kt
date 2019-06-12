package entity

import model.Model
import model.NpcLoader
import net.runelite.cache.definitions.NpcDefinition
import org.joml.Vector3f

val ENTITY_POS = Vector3f(0f, 0f, 0f)
val ENTITY_ROT = Vector3f(0f, 0f, 0f)
const val ENTITY_SCALE = 0.05f

class Entity(var model: Model, val npc: NpcDefinition, references: IntArray) {

    val position = ENTITY_POS
    val rotation = ENTITY_ROT
    val scale = ENTITY_SCALE
    val composition = ArrayList<Int>()

    init {
        for (reference in references) {
            composition.add(reference)
        }
    }

    fun remove(model: Int, npcLoader: NpcLoader) {
        composition.remove(model)
        reload(npcLoader)
    }

    fun reload(npcLoader: NpcLoader) {
        npcLoader.process(npc, composition)
    }
}