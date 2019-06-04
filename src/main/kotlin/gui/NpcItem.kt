package gui

import Processor
import net.runelite.cache.definitions.NpcDefinition
import org.liquidengine.legui.style.Style

class NpcItem(var npc: NpcDefinition, private val context: Processor, x: Float, y: Float, width: Float, height: Float)
    : Item(x, y, width, height) {

    init {
        updateText()
    }

    override fun updateText() {
        textState.text = npc.name
        style.display = Style.DisplayType.FLEX
    }

    override fun onClickEvent() {
        context.selectNpc(npc)
    }
}