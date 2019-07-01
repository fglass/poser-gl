package entity

import model.Model
import model.EntityHandler
import net.runelite.cache.definitions.NpcDefinition
import org.joml.Vector3f

val ENTITY_POS = Vector3f(0f, 0f, 0f)
val ENTITY_ROT = Vector3f(0f, 0f, 0f)
const val ENTITY_SCALE = 1f

class Entity(var model: Model, private val npc: NpcDefinition, references: IntArray) {

    val position = ENTITY_POS
    val rotation = ENTITY_ROT
    val scale = ENTITY_SCALE
    val composition = ArrayList<Int>()

    init {
        for (reference in references) {
            composition.add(reference)
        }
    }

    fun add(models: IntArray, entityHandler: EntityHandler) {
        models.filter { it != -1 }.forEach { composition.add(it) }
        reload(entityHandler)
    }

    fun remove(model: Int, entityHandler: EntityHandler) {
        composition.remove(model)
        reload(entityHandler)
    }

    fun reload(entityHandler: EntityHandler) {
        entityHandler.process(npc, composition)
    }
}