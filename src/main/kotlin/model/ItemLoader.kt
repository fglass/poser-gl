package model

import CACHE_PATH
import Processor
import net.runelite.cache.ItemManager
import net.runelite.cache.definitions.ItemDefinition
import net.runelite.cache.fs.Store
import java.io.File

class ItemLoader() {

    val items = ArrayList<ItemDefinition>()

    init {
        Store(File(CACHE_PATH)).use { store ->
            store.load()
            val manager = ItemManager(store)
            manager.load()

            for (item in manager.items) {
                if (item == null || item.name.toLowerCase() == "null" || item.maleModel0 == -1) {
                    continue
                }
                items.add(item)
            }
        }
    }

}