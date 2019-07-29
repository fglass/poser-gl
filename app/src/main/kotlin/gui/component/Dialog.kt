package gui.component

import org.joml.Vector2f
import org.liquidengine.legui.component.Dialog
import org.liquidengine.legui.component.Label
import org.liquidengine.legui.component.optional.align.HorizontalAlign
import org.liquidengine.legui.event.WindowSizeEvent

open class Dialog(title: String, text: String, width: Float, height: Float): Dialog(title, width, height) {

    val message = Label(text, 0f, 15f, size.x, 15f)

    init {
        message.textState.horizontalAlign = HorizontalAlign.CENTER
        container.add(message)
        container.isFocusable = false
        isFocusable = false
        isResizable = false

        listenerMap.addListener(WindowSizeEvent::class.java) { event ->
            position = Vector2f((event.width - size.x) / 2f, (event.height - size.y) / 2f)
        }
    }
}