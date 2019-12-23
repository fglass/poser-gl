package gui.component

import org.liquidengine.legui.component.Button
import org.liquidengine.legui.event.MouseClickEvent
import org.liquidengine.legui.input.Mouse
import render.RenderContext

class ConfirmDialog(context: RenderContext, title: String, message: String, button: String,
                    private val action: () -> Unit) : Dialog(title, message, context, 260f, 95f) {

    init {
        val width = 60f
        val confirm = Button(button, size.x / 2 - width / 2, 45f, width, 15f)
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