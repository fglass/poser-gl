package gui.component

import org.liquidengine.legui.component.Dialog
import org.liquidengine.legui.component.Label
import org.liquidengine.legui.component.optional.align.HorizontalAlign

open class Popup(title: String, text: String, width: Float, height: Float): Dialog(title, width, height) {

    val message = Label(text, 0f, 15f, size.x, 15f)

    init {
        message.textState.horizontalAlign = HorizontalAlign.CENTER
        container.add(message)
        container.isFocusable = false
        isFocusable = false
        isResizable = false
        titleTextState.horizontalAlign = HorizontalAlign.CENTER
    }
}