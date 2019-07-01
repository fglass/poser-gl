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

    fun load(entity: NpcDefinition) {
        val composition = ArrayList<EntityComponent>()
        entity.models.forEach {
            composition.add(EntityComponent(it, entity.recolorToFind, entity.recolorToReplace))
        }
        clear()
        process(composition)
    }

    fun process(composition: ArrayList<EntityComponent>) {
        val def = when {
            composition.size == 1 -> {
                context.datLoader.load(composition.first())
            } else -> {
                val defs = ArrayList<ModelDefinition>()
                composition.forEach { defs.add(context.datLoader.load(it)) }
                val merged = merge(defs)
                merged.computeNormals()
                merged
            }
        }
        def.computeAnimationTables()
        val model = context.datLoader.parse(def, context.framebuffer.shadingType == ShadingType.FLAT)
        context.entity = Entity(model, composition)
        context.gui.treePanel.update(context.entity!!)
    }

    private fun clear() {
        context.animationHandler.resetAnimation()
        context.entity = null
        context.loader.cleanUp()
    }
}