package input

import org.joml.Vector2f
import org.liquidengine.legui.event.MouseClickEvent
import org.liquidengine.legui.input.Mouse

class Mouse {

    var pressed = false
    var zooming = false
    var delta = Vector2f(0f, 0f)
    var dWheel = 0f

    fun handleClick(button: Mouse.MouseButton, action: MouseClickEvent.MouseClickAction) {
        if (button == Mouse.MouseButton.MOUSE_BUTTON_LEFT) {
            if (action == MouseClickEvent.MouseClickAction.PRESS) {
                pressed = true
                delta = Vector2f(0f, 0f)
            } else if (action == MouseClickEvent.MouseClickAction.RELEASE) {
                pressed = false
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
}