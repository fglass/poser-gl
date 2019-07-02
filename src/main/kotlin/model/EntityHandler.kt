package model

import Processor
import entity.Entity
import entity.EntityComponent
import model.ModelMerger.Companion.merge
import net.runelite.cache.definitions.ModelDefinition
import net.runelite.cache.definitions.NpcDefinition
import shader.ShadingType

class EntityHandler(private val context: Processor) {

    fun loadPlayer() {
        load(context.cacheService.entities[0]!!)
    }

    fun load(def: NpcDefinition) {
        val composition = ArrayList<EntityComponent>()
        def.models.forEach {
            composition.add(EntityComponent(it, def.recolorToFind, def.recolorToReplace))
        }
        clear()
        process(def.name, composition)
    }

    fun process(name: String, composition: ArrayList<EntityComponent>) {
        val def = when {
            composition.size == 1 -> {
                context.cacheService.loadModelDefinition(composition.first())
            } else -> {
                val defs = ArrayList<ModelDefinition>()
                composition.forEach { defs.add(context.cacheService.loadModelDefinition(it)) }
                val merged = merge(defs)
                merged.computeNormals()
                merged
            }
        }
        def.computeAnimationTables()
        val model = context.modelParser.parse(def, context.framebuffer.shadingType == ShadingType.FLAT)
        context.entity = Entity(name, model, composition)
        context.gui.managerPanel.update(context.entity!!)
    }

    private fun clear() {
        context.animationHandler.resetAnimation()
        context.entity = null
        context.loader.cleanUp()
    }
}