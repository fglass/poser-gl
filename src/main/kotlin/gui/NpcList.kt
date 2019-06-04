package gui

import Processor
import net.runelite.cache.NpcManager
import org.joml.Vector2f
import org.liquidengine.legui.component.ScrollablePanel

class NpcList(x: Float, y: Float, private val gui: Gui, context: Processor, npcManager: NpcManager): ScrollablePanel() {

    var maxIndex = 0
    private val listX = 2f
    private val listY = 2f
    private val listYOffset = 17

    init {
        position.x = x
        position.y = y
        size = getListSize()
        remove(horizontalScrollBar)

        var index = 0
        for (i in 0 until npcManager.npcs.size) {
            val npc = npcManager.get(i)

            if (npc == null || npc.name == "null") {
                continue
            }

            val npcItem = NpcItem(npc, context, listX, listY + index++ * listYOffset, 137f, 14f)
            npcItem.addListener()
            gui.npcItems.add(npcItem)
            container.add(npcItem)
            maxIndex = i
        }
        container.setSize(142f, listY + index * listYOffset)
        println("Loaded $index npcs")
    }

    fun getListSize(): Vector2f {
        return Vector2f(150f, gui.size.y - 52)
    }

    fun adjustScroll(filteredSize: Int) {
        verticalScrollBar.curValue = 0f // Reset scroll position
        container.setSize(142f, listY + filteredSize * listYOffset) // Adjust scroll size
    }

}