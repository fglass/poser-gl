package gui.component

import org.liquidengine.legui.component.Dialog
import org.liquidengine.legui.component.Label
import org.liquidengine.legui.component.optional.align.HorizontalAlign

class Popup(title: String, message: String, width: Float, height: Float): Dialog(title, width, height) {

    init {
        val label = Label(message, 0f, 15f, size.x, 15f)
        label.textState.horizontalAlign = HorizontalAlign.CENTER
        container.add(label)
        container.isFocusable = false
        isResizable = false
        titleTextState.horizontalAlign = HorizontalAlign.CENTER
    }
}