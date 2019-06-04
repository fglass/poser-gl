package gui

import Processor
import net.runelite.cache.NpcManager
import org.liquidengine.legui.component.TextInput

class NpcList(x: Float, y: Float, gui: Gui, context: Processor, private val npcManager: NpcManager):
    ItemList(x, y, gui) {

    var maxIndex = 0

    init {
        var index = 0
        for (i in 0 until npcManager.npcs.size) {
            val npc = npcManager.get(i)

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

    override fun search(searchField: TextInput) {
        val filtered = (0..maxIndex).toList().filter {
            val npc = npcManager.get(it)
            npc.name != "null" && npc.name.toLowerCase().contains(searchField.textState.text)
        }
        adjustScroll(filtered.size)

        for (i in 0 until gui.npcItems.size) {
            val item = gui.npcItems[i]
            when {
                filtered.size >= maxIndex -> { // Reset as no matches
                    val npc = npcManager.get(i)
                    item.npc = npc
                    item.updateText()
                }
                i < filtered.size -> { // Shift matches up
                    val npc = npcManager.get(filtered[i])
                    item.npc = npc
                    item.updateText()
                }
                else -> item.hide() // Hide filtered
            }
        }
    }
}