package entity

import Processor
import model.ModelMerger.Companion.merge
import net.runelite.cache.definitions.ModelDefinition
import net.runelite.cache.definitions.NpcDefinition
import shader.ShadingType

class EntityHandler(private val context: Processor) {

    fun loadPlayer() {
        load(context.cacheService.entities[-1]!!)
    }

    fun load(def: NpcDefinition) {
        val composition = HashSet<EntityComponent>()
        def.models.forEach {
            composition.add(EntityComponent(it, def.recolorToFind, def.recolorToReplace))
        }
        clear()
        process(def.name, composition)
    }

    fun process(name: String, composition: HashSet<EntityComponent>) {
        println("test")
        val def = when {
            composition.size == 1 -> {
                context.cacheService.loadModelDefinition(composition.first())
            } else -> {
                val defs = ArrayList<ModelDefinition>()
                composition.forEach { defs.add(context.cacheService.loadModelDefinition(it)) }
                merge(defs)
            }
        }

        def.computeAnimationTables()
        val model = context.modelParser.parse(def, context.framebuffer.shadingType == ShadingType.FLAT)
        context.entity = Entity(name, model, composition)
        context.gui.managerPanel.update(context.entity!!)
    }

    private fun clear() {
        context.entity = null
        context.animationHandler.resetAnimation()
        context.loader.cleanUp()
    }
}