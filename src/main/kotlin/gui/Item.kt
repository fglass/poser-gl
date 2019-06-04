package gui

import org.liquidengine.legui.component.Button
import org.liquidengine.legui.event.MouseClickEvent
import org.liquidengine.legui.style.Style

abstract class Item(x: Float, y: Float, width: Float, height: Float): Button(x, y, width, height) {

    abstract fun updateText()

    fun addClickListener() {
        listenerMap.addListener(MouseClickEvent::class.java) { event ->
            if (MouseClickEvent.MouseClickAction.CLICK == event.action) {
                onClickEvent()
            }
        }
    }

    abstract fun onClickEvent()

    fun hide() {
        style.display = Style.DisplayType.NONE
    }

}