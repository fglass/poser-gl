package gui.component

import Processor
import animation.Animation
import org.liquidengine.legui.component.Label
import org.liquidengine.legui.component.TextInput
import org.liquidengine.legui.component.event.widget.WidgetCloseEvent
import org.liquidengine.legui.component.optional.align.HorizontalAlign

class SequenceDialog(private val context: Processor, private val animation: Animation):
      Dialog("Sequence ${animation.sequence.id}", "", 260f, 69f) {

    init {
        isDraggable = false
        addAttributes()
    }

    private fun addAttributes() {
        val mainHandLabel = Label("Main Hand:", 24f, 7f, 45f, 15f)
        mainHandLabel.textState.horizontalAlign = HorizontalAlign.RIGHT
        mainHandLabel.style.focusedStrokeColor = null
        container.add(mainHandLabel)

        val mainHandId = TextInput(animation.sequence.leftHandItem.toString(), 76f, 7f, 65f, 15f)
        mainHandId.style.focusedStrokeColor = null
        container.add(mainHandId)

        val offHandLabel = Label("Off Hand:", 24f, 27f, 45f, 15f)
        offHandLabel.textState.horizontalAlign = HorizontalAlign.RIGHT
        offHandLabel.style.focusedStrokeColor = null
        container.add(offHandLabel)

        val offHandId = TextInput(animation.sequence.rightHandItem.toString(), 76f, 27f, 65f, 15f)
        offHandId.style.focusedStrokeColor = null
        container.add(offHandId)

        listenerMap.addListener(WidgetCloseEvent::class.java) {
            val mainHand = mainHandId.textState.text.toIntOrNull()?: -1
            val offHand = offHandId.textState.text.toIntOrNull()?: -1

            // Only modify on valid change
            if ((mainHand != animation.sequence.leftHandItem || offHand != animation.sequence.rightHandItem)
                && validItem(mainHand) && validItem(offHand)) {
                val animation = context.animationHandler.getAnimation()?: return@addListener
                animation.sequence.leftHandItem = mainHand
                animation.sequence.rightHandItem = offHand
                animation.equipItems()
            }
        }
    }

    private fun validItem(id: Int): Boolean {
        val offset = 512
        return id == -1 || context.cacheService.items[id - offset] != null
    }
}