package gui

import org.joml.Vector2f
import org.liquidengine.legui.component.Panel
import org.liquidengine.legui.style.color.ColorConstants

class AnimationPanel(private val gui: Gui) : Panel() {

    init {
        resize()
        style.background.color = ColorConstants.darkGray()
    }

    fun resize() {
        position = getPanelPosition()
        size = getPanelSize()
    }

    private fun getPanelPosition(): Vector2f {
        return Vector2f(5f, gui.size.y - 105)
    }

    private fun getPanelSize(): Vector2f {
        return Vector2f(gui.size.x - 10, 100f)
    }
}