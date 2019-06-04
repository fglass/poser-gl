package model

import CACHE_PATH
import Processor
import net.runelite.cache.NpcManager
import net.runelite.cache.definitions.NpcDefinition
import net.runelite.cache.fs.Store
import shader.ShadingType
import java.io.File

class NpcLoader(private val context: Processor) {

    private var currentNpc = NpcDefinition(-1)
    lateinit var manager: NpcManager

    init {
        Store(File(CACHE_PATH)).use { store ->
            store.load()
            manager = NpcManager(store)
            manager.load()
        }
    }

    fun load(npc: NpcDefinition) {
        clear()
        currentNpc = npc
        npc.models.forEach {
            val model = context.datLoader.load(it, context.shading == ShadingType.FLAT)
            context.addModel(model)
        }
        context.gui.infoPanel.update(npc)
    }

    fun reload() {
        load(currentNpc)
    }

    private fun clear() {
        context.animationHandler.resetAnimation()
        context.entities.clear()
        context.loader.cleanUp()
    }
}