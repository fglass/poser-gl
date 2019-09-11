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
      Dialog("Sequence Manager", "", context, 260f, 89f) {

    init {
        isDraggable = false
        addAttributes()
    }

    private fun addAttributes() {
        val idInput = addAttribute("Id", animation.sequence.id, 7f)
        val mainHandInput = addAttribute("Main Hand", getItemId(animation.sequence.leftHandItem), 27f)
        val offHandInput = addAttribute("Off Hand", getItemId(animation.sequence.rightHandItem), 47f)

        listenerMap.addListener(WidgetCloseEvent::class.java) {
            modifyId(idInput)
            animation.toggleItems(false) // Un-equip
            animation.sequence.leftHandItem = modifyItem(mainHandInput)
            animation.sequence.rightHandItem = modifyItem(offHandInput)
            animation.toggleItems(true) // Equip
        }
    }

    private fun addAttribute(name: String, value: Int, y: Float): TextInput {
        val label = Label("$name:", 24f, y, 45f, 15f)
        label.textState.horizontalAlign = HorizontalAlign.RIGHT
        label.style.focusedStrokeColor = null
        container.add(label)

        val input = TextInput(value.toString(), 79f, y, 65f, 15f)
        input.style.focusedStrokeColor = null
        container.add(input)
        return input
    }

    private fun getItemId(id: Int): Int {
        return max(id - ITEM_OFFSET, -1)
    }

    private fun modifyId(input: TextInput) {
        val id = input.textState.text.toIntOrNull()?: return
        if (context.cacheService.animations.contains(id)) { // Already in use
            return
        }
        val copied = Animation(id, animation)
        context.animationHandler.addAnimation(copied)
    }

    private fun modifyItem(input: TextInput): Int {
        val itemId = input.textState.text.toIntOrNull()?: return -1
        val item = context.cacheService.items.getOrElse(itemId) { return -1 }
        return item.id + ITEM_OFFSET
    }
}