package gui

import Processor
import net.runelite.cache.definitions.SequenceDefinition
import org.liquidengine.legui.style.Style

class AnimationItem(var sequence: SequenceDefinition, private val context: Processor, x: Float, y: Float, width: Float,
                    height: Float) : Item(x, y, width, height) {

    init {
        updateText()
    }

    override fun updateText() {
        textState.text = sequence.id.toString()
        style.display = Style.DisplayType.FLEX
    }

    override fun onClickEvent() {
        context.animationHandler.playAnimation(sequence)
        context.gui.infoWidget.animationId.textState.text = sequence.id.toString()
    }
}