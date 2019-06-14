package gui.component

import Processor
import gui.Gui
import net.runelite.cache.definitions.SequenceDefinition

class AnimationList(x: Float, y: Float, gui: Gui, private val context: Processor): ElementList(x, y, gui) {

    private val animationElements = mutableListOf<AnimationElement>()

    init {
        var index = 0
        for ((i, sequence) in context.animationHandler.sequences.values.withIndex()) {
            val element = AnimationElement(sequence, context, listX, listY + index++ * listYOffset, 137f, 14f)
            element.addClickListener()
            animationElements.add(element)
            container.add(element)
            maxIndex = i
        }
        container.setSize(142f, listY + index * listYOffset)
    }

    override fun getFiltered(input: String): List<Int> {
        return (0 until maxIndex).toList().filter {
            it.toString().contains(input)
        }
    }

    override fun getElements(): List<Element> {
        return animationElements
    }

    override fun handleElement(index: Int, element: Element) {
        val sequence = context.animationHandler.sequences[index]
        if (sequence != null && element is AnimationElement) {
            element.sequence = sequence
            element.updateText()
        }
    }

    class AnimationElement(var sequence: SequenceDefinition, private val context: Processor, x: Float, y: Float,
                           width: Float, height: Float): Element(x, y, width, height) {
        init {
            updateText()
        }

        override fun updateText() {
            textState.text = sequence.id.toString()
            isEnabled = true
        }

        override fun onClickEvent() {
            context.animationHandler.play(sequence)
        }
    }
}