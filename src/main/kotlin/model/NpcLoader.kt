package model

import CACHE_PATH
import Processor
import net.runelite.cache.NpcManager
import net.runelite.cache.definitions.NpcDefinition
import net.runelite.cache.fs.Store
import render.Loader
import shader.ShadingType
import java.io.File
import java.util.*

class NpcLoader {

    var current = ""
    lateinit var manager: NpcManager
    private val dictionary = HashMap<String, NpcDefinition>()

    init {
        Store(File(CACHE_PATH)).use { store ->
            store.load()
            manager = NpcManager(store)
            manager.load()

            for (id in 0 until manager.npcs.size) {
                val npc = manager.get(id)
                if (npc != null) {
                    dictionary[npc.name] = npc
                }
            }
        }
    }

    fun load(name: String, loader: Loader, context: Processor) {
        current = name
        val npc = dictionary[name]
        if (npc != null) {
            npc.models?.forEach {
                val model = context.datLoader.load(it, context.shading == ShadingType.FLAT, loader)
                context.addModel(model)
            }
        }
    }
}