package model

import CACHE_PATH
import Processor
import entity.Entity
import model.ModelMerger.Companion.merge
import net.runelite.cache.NpcManager
import net.runelite.cache.definitions.ModelDefinition
import net.runelite.cache.definitions.NpcDefinition
import net.runelite.cache.fs.Store
import shader.ShadingType
import java.io.File

class EntityLoader(private val context: Processor) {

    private val player = NpcDefinition(-1)
    val entities = ArrayList<NpcDefinition>()

    init {
        Store(File(CACHE_PATH)).use { store ->
            store.load()
            val manager = NpcManager(store)
            manager.load()
            addPlayer()
            for (npc in manager.npcs) {
                if (npc == null || npc.name == "null") {
                    continue
                }
                entities.add(npc)
            }
        }
    }

    private fun addPlayer() {
        player.name = "Player"
        player.models = intArrayOf(230, 249, 292, 151, 176, 254, 181)
        entities.add(player)
    }

    fun loadPlayer() {
        load(player)
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