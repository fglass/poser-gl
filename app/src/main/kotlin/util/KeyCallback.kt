package util

import org.liquidengine.legui.component.TextInput
import org.liquidengine.legui.system.context.Context
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWKeyCallbackI
import render.RenderContext

class KeyCallback(private val context: RenderContext, private val guiContext: Context) : GLFWKeyCallbackI {

    private val animationHandler = context.animationHandler
    private val mac = System.getProperty("os.name").startsWith("Mac")
    private val ctrl = if (mac) GLFW.GLFW_MOD_SUPER else GLFW.GLFW_MOD_CONTROL

    override fun invoke(window: Long, key: Int, scancode: Int, action: Int, mods: Int) {
        if (action == GLFW.GLFW_RELEASE || isTyping()) {
            return
        }

        when {
            key == GLFW.GLFW_KEY_Z && mods == ctrl -> animationHandler.history.undo()
            key == GLFW.GLFW_KEY_Z && mods == ctrl + GLFW.GLFW_MOD_SHIFT -> animationHandler.history.redo()
            key == GLFW.GLFW_KEY_E && mods == ctrl -> context.exportManager.redo()
            key == GLFW.GLFW_KEY_RIGHT -> animationHandler.setNextFrame()
            key == GLFW.GLFW_KEY_LEFT -> animationHandler.setPreviousFrame()
        }
    }

    private fun isTyping() = guiContext.focusedGui is TextInput
}