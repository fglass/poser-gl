package gui.component

import render.RenderContext
import animation.Animation
import animation.ITEM_OFFSET
import org.liquidengine.legui.component.Label
import org.liquidengine.legui.component.TextInput
import org.liquidengine.legui.component.event.widget.WidgetCloseEvent
import org.liquidengine.legui.component.optional.align.HorizontalAlign
import kotlin.math.max

class SequenceDialog(private val context: RenderContext, private val animation: Animation):
      Dialog("Sequence ${animation.sequence.id}", "", context, 260f, 69f) {

    init {
        isDraggable = false
        addAttributes()
    }

    private fun addAttributes() {
        val mainHandLabel = Label("Main Hand:", 24f, 7f, 45f, 15f)
        mainHandLabel.textState.horizontalAlign = HorizontalAlign.RIGHT
        mainHandLabel.style.focusedStrokeColor = null
        container.add(mainHandLabel)

        val mainHandId = TextInput(getItemId(animation.sequence.leftHandItem).toString(), 76f, 7f, 65f, 15f)
        mainHandId.style.focusedStrokeColor = null
        container.add(mainHandId)

        val offHandLabel = Label("Off Hand:", 24f, 27f, 45f, 15f)
        offHandLabel.textState.horizontalAlign = HorizontalAlign.RIGHT
        offHandLabel.style.focusedStrokeColor = null
        container.add(offHandLabel)

        val offHandId = TextInput(getItemId(animation.sequence.rightHandItem).toString(), 76f, 27f, 65f, 15f)
        offHandId.style.focusedStrokeColor = null
        container.add(offHandId)

        listenerMap.addListener(WidgetCloseEvent::class.java) {
            val mainHand = mainHandId.textState.text.toIntOrNull()?: -1
            val offHand = offHandId.textState.text.toIntOrNull()?: -1

            // Only modify on valid change
            if ((mainHand != getItemId(animation.sequence.leftHandItem) ||
                offHand != getItemId(animation.sequence.rightHandItem))
                && validItem(mainHand) && validItem(offHand)) {

                val animation = context.animationHandler.getAnimation()?: return@addListener
                animation.sequence.leftHandItem = mainHand + ITEM_OFFSET
                animation.sequence.rightHandItem = offHand + ITEM_OFFSET
                animation.equipItems()
            }
        }
    }

    private fun getItemId(id: Int): Int {
        return max(id - ITEM_OFFSET, -1)
    }

    private fun validItem(id: Int): Boolean {
        return id == -1 || context.cacheService.items[id] != null
    }
}