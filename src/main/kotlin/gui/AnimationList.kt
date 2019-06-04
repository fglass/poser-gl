package gui

import Processor
import org.liquidengine.legui.component.TextInput

class AnimationList(x: Float, y: Float, gui: Gui, private val context: Processor): ItemList(x, y, gui) {

    var maxIndex = 0

    init {
        var index = 0
        for (i in 0 until context.animationHandler.sequences.size) {
            val sequence = context.animationHandler.sequences[i]

            if (sequence != null) {
                val item = AnimationItem(sequence, context, listX, listY + index++ * listYOffset, 137f, 14f)
                item.addClickListener()
                gui.animationItems.add(item)
                container.add(item)
                maxIndex = i
            }
        }
        container.setSize(142f, listY + index * listYOffset)
    }

    override fun search(searchField: TextInput) {
        val filtered = (0..maxIndex).toList().filter {
            it.toString().contains(searchField.textState.text)
        }
        adjustScroll(filtered.size)

        for (i in 0 until gui.animationItems.size) {
            val item = gui.animationItems[i]
            when {
                filtered.size >= maxIndex -> {
                    val sequence = context.animationHandler.sequences[i]
                    if (sequence != null) {
                        item.sequence = sequence
                        item.updateText()
                    }
                }
                i < filtered.size -> {
                    val sequence = context.animationHandler.sequences[filtered[i]]
                    if (sequence != null) {
                        item.sequence = sequence
                        item.updateText()
                    }
                }
                else -> item.hide()
            }
        }
    }
}