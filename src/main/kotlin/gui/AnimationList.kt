package gui

import Processor
import net.runelite.cache.definitions.SequenceDefinition

class AnimationList(x: Float, y: Float, gui: Gui, private val context: Processor): ItemList(x, y, gui) {

    init {
        var index = 0
        for ((i, sequence) in context.animationHandler.sequences.values.withIndex()) {
            val item = AnimationItem(sequence, context, listX, listY + index++ * listYOffset, 137f, 14f)
            item.addClickListener()
            gui.animationItems.add(item)
            container.add(item)
            maxIndex = i
        }
        container.setSize(142f, listY + index * listYOffset)
    }

    override fun getFiltered(input: String): List<Int> {
        return (0 until maxIndex).toList().filter {
            it.toString().contains(input)
        }
    }

    override fun getItems(): List<Item> {
        return gui.animationItems
    }

    override fun handleItem(index: Int, item: Item) {
        val sequence = context.animationHandler.sequences[index]
        if (sequence != null && item is AnimationItem) {
            item.sequence = sequence
            item.updateText()
        }
    }

    class AnimationItem(var sequence: SequenceDefinition, private val context: Processor, x: Float, y: Float,
                        width: Float, height: Float): Item(x, y, width, height) {

        init {
            updateText()
        }

        override fun updateText() {
            textState.text = sequence.id.toString()
            isEnabled = true
        }

        override fun onClickEvent() {
            context.animationHandler.playAnimation(sequence)
            context.gui.animationPanel.setTimeline(sequence)
        }
    }
}