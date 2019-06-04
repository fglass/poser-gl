package gui

import Processor
import net.runelite.cache.definitions.NpcDefinition

class NpcItem(var npc: NpcDefinition, private val context: Processor, x: Float, y: Float, width: Float, height: Float)
    : Item(x, y, width, height) {

    init {
        updateText()
    }

    override fun updateText() {
        textState.text = npc.name
        isEnabled = true
    }

    override fun onClickEvent() {
        context.npcLoader.load(npc)
    }
}