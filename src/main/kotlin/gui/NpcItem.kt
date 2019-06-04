package gui

import net.runelite.cache.definitions.NpcDefinition
import org.liquidengine.legui.component.Button
import org.liquidengine.legui.event.MouseClickEvent
import org.liquidengine.legui.style.Style
import Processor

class NpcItem(var npc: NpcDefinition, private val context: Processor, x: Float, y: Float, width: Float, height: Float)
    : Button(x, y, width, height) {

    init {
        updateText()
    }

    fun updateText() {
        textState.text = npc.name
        style.display = Style.DisplayType.FLEX
    }

    fun addListener() {
        listenerMap.addListener(MouseClickEvent::class.java) { event ->
            if (MouseClickEvent.MouseClickAction.CLICK == event.action) {
                context.selectNpc(npc)
            }
        }
    }

    fun hide() {
        style.display = Style.DisplayType.NONE
    }
}