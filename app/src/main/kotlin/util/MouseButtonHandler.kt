package util

import org.joml.Vector2f
import com.spinyowl.legui.event.MouseClickEvent
import com.spinyowl.legui.input.Mouse

class MouseButtonHandler(val button: Mouse.MouseButton) {

    var pressed = false
    var clicked = true
    var delta = Vector2f()

    fun handleClick(button: Mouse.MouseButton, action: MouseClickEvent.MouseClickAction) {
        if (this.button == button) {
            when (action) {
                MouseClickEvent.MouseClickAction.PRESS -> pressed = true
                MouseClickEvent.MouseClickAction.RELEASE -> reset()
                else -> clicked = true
            }
        }
    }

    fun handleDrag(delta: Vector2f) {
        if (pressed) {
            this.delta = delta
        }
    }

    fun handleCursorEvent(entered: Boolean) {
        if (!entered) {
            reset()
        }
    }

    private fun reset() {
        pressed = false
        delta = Vector2f()
    }
}