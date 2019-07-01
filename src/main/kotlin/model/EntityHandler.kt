package model

import Processor
import entity.Entity
import model.ModelMerger.Companion.merge
import net.runelite.cache.definitions.ModelDefinition
import net.runelite.cache.definitions.NpcDefinition
import shader.ShadingType

class EntityHandler(private val context: Processor) {

    fun loadPlayer() {
        load(context.cacheService.entities[0]!!)
    }

    fun load(entity: NpcDefinition) {
        clear()
        process(entity, entity.models.asList())
    }

    fun process(entity: NpcDefinition, models: List<Int>) { // TODO: Clean-up
        if (models.size == 1) {
            val modelId = models[0]
            val def = context.datLoader.load(modelId, entity)
            def.computeAnimationTables()
            val model = context.datLoader.parse(def, context.framebuffer.shadingType == ShadingType.FLAT)
            context.entity = Entity(model, entity, intArrayOf(modelId))
        } else {
            val defs = ArrayList<ModelDefinition>()
            models.forEach {
                defs.add(context.datLoader.load(it, entity))
            }
            val merged = merge(defs)
            merged.computeNormals()
            merged.computeAnimationTables()
            val model = context.datLoader.parse(merged, context.framebuffer.shadingType == ShadingType.FLAT)
            context.entity = Entity(model, entity, models.toIntArray())
        }
        context.gui.treePanel.update(context.entity!!)
    }

    private fun clear() {
        context.animationHandler.resetAnimation()
        context.entity = null
        context.loader.cleanUp()
    }
}