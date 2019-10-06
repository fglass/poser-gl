package entity

import render.RenderContext
import model.ModelMerger.Companion.merge
import net.runelite.cache.definitions.ModelDefinition
import net.runelite.cache.definitions.NpcDefinition
import shader.ShadingType

class EntityHandler(private val context: RenderContext) {

    fun loadPlayer() {
        load(context.cacheService.entities[-1]!!)
    }

    fun load(def: NpcDefinition) {
        val composition = HashSet<EntityComponent>()
        def.models.forEach {
            composition.add(EntityComponent(it, def.recolorToFind, def.recolorToReplace))
        }
        clear()
        matchAnimations(def)
        process(def.name, def.tileSpacesOccupied, composition)
    }

    private fun matchAnimations(def: NpcDefinition) {
        val walk = context.cacheService.animations[def.walkAnimation]
        val siblings = walk?.findSiblings()?: emptyArray<Int>().toIntArray()
        context.gui.listPanel.animationList.highlighted = siblings
        context.gui.listPanel.animationList.reset()
    }

    fun process(name: String, size: Int, composition: HashSet<EntityComponent>) {
        val def = when {
            composition.size == 1 -> context.cacheService.loadModelDefinition(composition.first())
            else -> {
                val defs = ArrayList<ModelDefinition>()
                composition.forEach { defs.add(context.cacheService.loadModelDefinition(it)) }
                merge(defs)
            }
        }
        def.computeAnimationTables()

        val model = context.modelParser.parse(def, context.framebuffer.shadingType == ShadingType.FLAT)
        context.entity = Entity(name, size, model, composition)
        context.lineRenderer.setGrid(size)
        context.gui.managerPanel.update(context.entity!!)
        context.gui.listPanel.animationList.verticalScrollBar.curValue = 0f // Reset scroll
    }

    private fun clear() {
        context.entity = null
        context.animationHandler.resetAnimation()
        context.modelParser.cleanUp()
    }
}