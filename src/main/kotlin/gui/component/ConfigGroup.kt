package gui.component

import org.joml.Vector2f
import org.liquidengine.legui.event.MouseClickEvent
import org.liquidengine.legui.image.Image
import org.liquidengine.legui.input.Mouse
import org.liquidengine.legui.style.color.ColorConstants

class ConfigGroup(position: Vector2f, iconSize: Vector2f, vararg images: Image):
      ButtonGroup(position, iconSize, *images) {

    init {
        buttons.forEach {
            if (it == buttons[0]) {
                updateConfigs(it)
            }

            it.listenerMap.addListener(MouseClickEvent::class.java) { event ->
                if (event.button == Mouse.MouseButton.MOUSE_BUTTON_LEFT &&
                    event.action == MouseClickEvent.MouseClickAction.CLICK) {
                    updateConfigs(it)
                }
            }
        }
    }

    fun updateConfigs(selected: ImageButton) {
        buttons.forEach { it.style.background.color =
            when {
                !it.isFocusable -> ColorConstants.lightRed()
                it == selected -> ColorConstants.gray()
                else -> ColorConstants.transparent()
            }
        }
    }
}