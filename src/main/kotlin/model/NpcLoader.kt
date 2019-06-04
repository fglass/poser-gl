package model

import CACHE_PATH
import Processor
import net.runelite.cache.NpcManager
import net.runelite.cache.definitions.NpcDefinition
import net.runelite.cache.fs.Store
import shader.ShadingType
import java.io.File

class NpcLoader(private val context: Processor) {

    var currentNpc = NpcDefinition(-1)
    lateinit var manager: NpcManager

    init {
        Store(File(CACHE_PATH)).use { store ->
            store.load()
            manager = NpcManager(store)
            manager.load()
        }
    }

    fun load(npc: NpcDefinition) {
        currentNpc = npc
        npc.models.forEach {
            val model = context.datLoader.load(it, context.shading == ShadingType.FLAT)
            context.addModel(model)
        }
    }
}