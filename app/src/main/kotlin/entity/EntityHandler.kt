package entity

import api.definition.ModelDefinition
import api.definition.NpcDefinition
import model.ModelMerger.Companion.merge
import render.RenderContext
import shader.ShadingType

class EntityHandler(private val context: RenderContext) {

    var entity: Entity? = null
    var scale = 1f

    fun loadPlayer() {
        val player = context.cacheService.entities.getOrElse(-1) { context.cacheService.entities.values.first() }
        load(player)
    }

    fun load(def: NpcDefinition) {
        def.models?.let { models ->
            val composition = HashSet<EntityComponent>()
            composition.addAll(models.map { EntityComponent(it, def.originalColours, def.newColours) })
            clear()
            process(def.name, def.spaces, composition)
        }
    }

    fun process(name: String, size: Int, composition: HashSet<EntityComponent>) {
        val def = when (composition.size) {
            1 -> context.cacheService.loadModelDef(composition.first())
            else -> {
                val defs = ArrayList<ModelDefinition>()
                defs.addAll(composition.map(context.cacheService::loadModelDef))
                merge(defs)
            }
        }
        def.computeAnimationTables()

        val model = context.modelParser.parse(def, context.framebuffer.shadingType == ShadingType.FLAT)
        entity = Entity(name, size, scale, model, composition)
        context.lineRenderer.setGrid(size)
        entity?.let(context.gui.managerPanel::update)
        context.gui.listPanel.animationList.verticalScrollBar.curValue = 0f // Reset scroll
    }

    private fun ModelDefinition.computeAnimationTables() {
        vertexSkins?.let { skins ->
            val groupCounts = IntArray(256)
            var numGroups = 0

            repeat(vertexCount) {
                val temp = skins[it]
                groupCounts[temp]++
                if (temp > numGroups) {
                    numGroups = temp
                }
            }

            vertexGroups = Array(numGroups + 1) { IntArray(0) }
            repeat(numGroups + 1) {
                vertexGroups[it] = IntArray(groupCounts[it])
                groupCounts[it] = 0
            }

            repeat(vertexCount) {
                val temp = skins[it]
                vertexGroups[temp][groupCounts[temp]++] = it
            }
            vertexSkins = null
        }
    }

    fun clear() {
        entity = null
        context.animationHandler.resetAnimation()
        context.modelParser.cleanUp()
    }
}