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

    lateinit var manager: NpcManager
    private val dictionary = HashMap<String, NpcDefinition>()
    private val datLoader = DatLoader()

    init {
        Store(File(CACHE_PATH)).use { store ->
            store.load()
            manager = NpcManager(store)
            manager.load()

            for (id in 0 until manager.npcs.size) {
                dictionary[manager.get(id).name] = manager.get(id)
            }
        }
    }

    fun load(name: String, loader: Loader, context: Processor) {
        val npc = dictionary[name]
        if (npc != null) {
            npc.models?.forEach {
                val model = datLoader.load(it, context.shading == ShadingType.FLAT, loader)
                context.addModel(model)
            }
        }
    }
}