package gui

import org.liquidengine.legui.component.Button
import org.liquidengine.legui.event.MouseClickEvent
import org.liquidengine.legui.style.color.ColorConstants

abstract class Item(x: Float, y: Float, width: Float, height: Float): Button(x, y, width, height) {

    init {
        style.background.color = ColorConstants.darkGray()
        style.border.isEnabled = false
    }

    abstract fun updateText()

    fun addClickListener() {
        listenerMap.addListener(MouseClickEvent::class.java) { event ->
            if (MouseClickEvent.MouseClickAction.CLICK == event.action) {
                onClickEvent()
            }
        }
    }

    abstract fun onClickEvent()
}