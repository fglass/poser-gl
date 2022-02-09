package gui.component

import org.joml.Vector2f
import com.spinyowl.legui.event.MouseClickEvent
import com.spinyowl.legui.image.Image
import com.spinyowl.legui.input.Mouse
import com.spinyowl.legui.style.color.ColorConstants

class ConfigGroup(position: Vector2f, iconSize: Vector2f, images: Array<Image>, actions: Array<String>, padding: Int = 2):
      ButtonGroup(position, iconSize, images, actions, padding) {

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