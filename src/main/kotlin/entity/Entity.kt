package entity

import model.Model
import model.EntityHandler
import net.runelite.cache.definitions.ItemDefinition
import net.runelite.cache.definitions.NpcDefinition
import org.joml.Vector3f

val ENTITY_POS = Vector3f(0f, 0f, 0f)
val ENTITY_ROT = Vector3f(0f, 0f, 0f)
const val ENTITY_SCALE = 1f

class Entity(var model: Model, val composition: ArrayList<EntityComponent>) {

    val position = ENTITY_POS
    val rotation = ENTITY_ROT
    val scale = ENTITY_SCALE


    fun addItem(item: ItemDefinition, entityHandler: EntityHandler) {
        val models = intArrayOf(item.maleModel0, item.maleModel1, item.maleModel2)
        models.filter { it != -1 }.forEach { composition.add((EntityComponent(it, item.colorFind, item.colorReplace))) }
        reload(entityHandler)
    }

    fun remove(component: EntityComponent, entityHandler: EntityHandler) {
        composition.remove(component)
        reload(entityHandler)
    }

    fun reload(entityHandler: EntityHandler) {
        entityHandler.process(composition)
    }
}

class EntityComponent(val id: Int, val originalColours: ShortArray?, val newColours: ShortArray?)