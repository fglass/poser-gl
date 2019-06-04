package gui

import Processor
import net.runelite.cache.definitions.SequenceDefinition

class AnimationItem(var sequence: SequenceDefinition, private val context: Processor, x: Float, y: Float, width: Float,
                    height: Float) : Item(x, y, width, height) {

    init {
        updateText()
    }

    override fun updateText() {
        textState.text = sequence.id.toString()
        isEnabled = true
    }

    override fun onClickEvent() {
        context.animationHandler.playAnimation(sequence)
        context.gui.infoPanel.animationId.textState.text = sequence.id.toString()
    }
}