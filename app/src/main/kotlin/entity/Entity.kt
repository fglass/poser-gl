package entity

import model.Model
import net.runelite.cache.definitions.ItemDefinition
import org.joml.Vector3f
import java.util.*
import kotlin.collections.HashSet

val ENTITY_POS = Vector3f(0f, 0f, 0f)
val ENTITY_ROT = Vector3f(0f, 0f, 0f)
const val ENTITY_SCALE = 1f

class Entity(val name: String, val size: Int, var model: Model, val composition: HashSet<EntityComponent>) {

    val position = ENTITY_POS
    val rotation = ENTITY_ROT
    val scale = ENTITY_SCALE

    fun addItem(item: ItemDefinition, entityHandler: EntityHandler) {
        val size = composition.size
        val models = intArrayOf(item.maleModel0, item.maleModel1, item.maleModel2)
        models.filter { it > 0 }.forEach { composition.add((EntityComponent(it, item.colorFind, item.colorReplace))) }

        if (composition.size > size) { // Only reload if composition has changed
            reload(entityHandler)
        }
    }

    fun remove(component: EntityComponent, entityHandler: EntityHandler) {
        composition.remove(component)
        reload(entityHandler)
    }

    fun reload(entityHandler: EntityHandler) {
        entityHandler.process(name, size, composition)
    }
}

class EntityComponent(val id: Int, val originalColours: ShortArray?, val newColours: ShortArray?) {

    override fun equals(other: Any?): Boolean {
        other as EntityComponent
        return id == other.id
    }

    override fun hashCode(): Int {
        return Objects.hashCode(id)
    }
}