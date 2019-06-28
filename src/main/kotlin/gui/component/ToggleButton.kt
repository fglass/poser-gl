package gui.component

import org.joml.Vector2f
import org.liquidengine.legui.event.MouseClickEvent
import org.liquidengine.legui.image.Image
import org.liquidengine.legui.input.Mouse
import org.liquidengine.legui.style.color.ColorConstants

class ToggleButton(position: Vector2f, size: Vector2f, image: Image, toggled: Boolean) : ImageButton(position, image) {

    init {
        this.size = size
        if (toggled) {
            style.background.color = ColorConstants.gray()
        }

        listenerMap.addListener(MouseClickEvent::class.java) { event ->
            if (event.button == Mouse.MouseButton.MOUSE_BUTTON_LEFT &&
                event.action == MouseClickEvent.MouseClickAction.CLICK) {

                style.background.color = when {
                    !isFocusable -> ColorConstants.lightRed()
                    style.background.color == ColorConstants.transparent() -> ColorConstants.gray()
                    else -> ColorConstants.transparent()
                }
            }
        }
    }
}