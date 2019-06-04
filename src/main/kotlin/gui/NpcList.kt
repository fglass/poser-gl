package gui

import Processor
import net.runelite.cache.NpcManager

class NpcList(x: Float, y: Float, gui: Gui, context: Processor, private val npcManager: NpcManager):
    ItemList(x, y, gui) {

    init {
        var index = 0
        for ((i, npc) in npcManager.npcs.withIndex()) {
            if (npc == null || npc.name == "null") {
                continue
            }

            val item = NpcItem(npc, context, listX, listY + index++ * listYOffset, 137f, 14f)
            item.addClickListener()
            gui.npcItems.add(item)
            container.add(item)
            maxIndex = i
        }
        container.setSize(142f, listY + index * listYOffset)
        println("Loaded $index npcs")
    }

    override fun getFiltered(input: String): List<Int> {
        return (0 until maxIndex).toList().filter {
            val npc = npcManager.get(it)
            npc.name != "null" && npc.name.toLowerCase().contains(input)
        }
    }

    override fun getItems(): List<Item> {
        return gui.npcItems
    }

    override fun handleItem(index: Int, item: Item) {
        val npc = npcManager.get(index)
        if (item is NpcItem) {
            item.npc = npc
            item.updateText()
        }
    }
}