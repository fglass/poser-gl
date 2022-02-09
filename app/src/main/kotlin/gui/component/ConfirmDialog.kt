package gui.component

import com.spinyowl.legui.component.Button
import com.spinyowl.legui.event.MouseClickEvent
import com.spinyowl.legui.input.Mouse
import render.RenderContext

class ConfirmDialog(context: RenderContext, title: String, message: String, button: String,
                    private val action: () -> Unit) : Dialog(title, message, context, 260f, 95f) {

    init {
        val width = 60f
        val confirm = Button(button, size.x / 2 - width / 2, 45f, width, 15f)
        confirm.style.focusedStrokeColor = null
        confirm.listenerMap.addListener(MouseClickEvent::class.java) { event ->
            if (event.button == Mouse.MouseButton.MOUSE_BUTTON_LEFT &&
                event.action == MouseClickEvent.MouseClickAction.CLICK) {
                close()
                action.invoke()
            }
        }
        container.add(confirm)
    }
}