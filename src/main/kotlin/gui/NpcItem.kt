package gui

import net.runelite.cache.definitions.NpcDefinition
import org.liquidengine.legui.component.Button
import org.liquidengine.legui.style.Style

class NpcItem(var npc: NpcDefinition, x: Float, y: Float, width: Float, height: Float) : Button(x, y, width, height) {

    init {
        updateText()
    }

    fun updateText() {
        textState.text = npc.name
        style.display = Style.DisplayType.FLEX
    }
}