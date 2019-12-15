package util

import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWKeyCallbackI
import render.RenderContext

class KeyCallback(context: RenderContext) : GLFWKeyCallbackI {

    private val animationHandler = context.animationHandler
    private val mac = System.getProperty("os.name").startsWith("Mac")
    private val ctrl = if (mac) GLFW.GLFW_MOD_SUPER else GLFW.GLFW_MOD_CONTROL

    override fun invoke(window: Long, key: Int, scancode: Int, action: Int, mods: Int) {
        if (action != GLFW.GLFW_PRESS && action != GLFW.GLFW_REPEAT) {
            return
        }

        when {
            mods == ctrl && key == GLFW.GLFW_KEY_Z -> animationHandler.history.undo()
            mods == ctrl + GLFW.GLFW_MOD_SHIFT && key == GLFW.GLFW_KEY_Z -> animationHandler.history.redo()
            key == GLFW.GLFW_KEY_RIGHT -> animationHandler.setNextFrame()
            key == GLFW.GLFW_KEY_LEFT -> animationHandler.setPreviousFrame()
        }
    }
}