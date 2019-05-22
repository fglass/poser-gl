package input

import org.joml.Vector2f
import org.lwjgl.glfw.GLFW.*

class Mouse {

    var pressed = false
    private var previousPosition = Vector2f(0f, 0f)
    private var newPosition = Vector2f(0f, 0f)
    private var previousDWheel = 0f
    var dWheel = 0f

    fun handleClick(button: Int, action:Int) {
        if (button == GLFW_MOUSE_BUTTON_LEFT) {
            if (action == GLFW_PRESS) {
                pressed = true
            } else if (action == GLFW_RELEASE) {
                pressed = false
            }
        }
    }

    fun handlePosition(x: Double, y: Double) {
        previousPosition = newPosition
        newPosition = Vector2f(x.toFloat(), y.toFloat())
        dWheel = 0f // Prevent infinite scrolling
    }

    fun handleScroll(dx: Double, dy: Double) {
        previousDWheel = dWheel
        dWheel = if (previousDWheel == dy.toFloat()) {
            0f
        } else {
            dy.toFloat()
        }
    }

    fun getDX(): Float {
        return newPosition.x - previousPosition.x
    }

    fun getDY(): Float {
        return newPosition.y - previousPosition.y
    }
}