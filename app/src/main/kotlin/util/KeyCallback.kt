package util

import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWKeyCallbackI
import render.RenderContext

class KeyCallback(private val context: RenderContext) : GLFWKeyCallbackI {

    private val animationHandler = context.animationHandler
    private val mac = System.getProperty("os.name").startsWith("Mac")
    private val ctrl = if (mac) GLFW.GLFW_MOD_SUPER else GLFW.GLFW_MOD_CONTROL

    override fun invoke(window: Long, key: Int, scancode: Int, action: Int, mods: Int) {
        when (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT) {
            key == GLFW.GLFW_KEY_Z && mods == ctrl -> animationHandler.history.undo()
            key == GLFW.GLFW_KEY_Z && mods == ctrl + GLFW.GLFW_MOD_SHIFT -> animationHandler.history.redo()
            key == GLFW.GLFW_KEY_E && mods == ctrl -> context.exportManager.redo()
            key == GLFW.GLFW_KEY_RIGHT -> animationHandler.setNextFrame() // TODO: only if framebuffer focused
            key == GLFW.GLFW_KEY_LEFT -> animationHandler.setPreviousFrame()
        }
    }
}