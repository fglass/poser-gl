package util

import org.joml.Vector2f
import org.liquidengine.legui.event.MouseClickEvent
import org.liquidengine.legui.input.Mouse

class MouseHandler(private val button: Mouse.MouseButton) {

    var pressed = false
    var clicked = true
    var zooming = false
    var delta = Vector2f()
    var dWheel = 0f

    fun handleClick(button: Mouse.MouseButton, action: MouseClickEvent.MouseClickAction) {
        if (this.button == button) {
            when (action) {
                MouseClickEvent.MouseClickAction.PRESS -> {
                    pressed = true
                    delta = Vector2f()
                }
                MouseClickEvent.MouseClickAction.RELEASE -> pressed = false
                else -> clicked = true
            }
        }
    }

    fun handleDrag(delta: Vector2f) {
        this.delta = delta
    }

    fun handleScroll(dWheel: Double) {
        zooming = true
        this.dWheel = dWheel.toFloat()
    }

    fun handleCursorEvent(entered: Boolean) {
        if (!entered) {
            pressed = false
        }
    }
}