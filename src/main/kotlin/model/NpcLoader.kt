package model

import CACHE_PATH
import Processor
import net.openrs.cache.Cache
import net.openrs.cache.FileStore
import net.openrs.cache.type.npcs.NpcType
import net.openrs.cache.type.npcs.NpcTypeList
import render.Loader
import java.io.File
import java.util.*

class NpcLoader {

    lateinit var list: NpcTypeList
    private val dictionary = HashMap<String, NpcType>()
    private val datLoader = DatLoader()

    init {
        Cache(FileStore.open(File(CACHE_PATH))).use { cache ->
            list = NpcTypeList()
            list.initialize(cache)

            for (id in 0 until list.size()) {
                dictionary[list.list(id).name] = list.list(id)
            }
        }
    }

    fun load(name: String, loader: Loader, context: Processor) {
        val npc = dictionary[name]
        if (npc != null) {
            npc.models?.forEach {
                val model = datLoader.load(it, loader)
                context.addModel(model)
            }
        }
    }
}