package gui.component

import org.joml.Vector2f
import com.spinyowl.legui.event.MouseClickEvent
import com.spinyowl.legui.image.Image
import com.spinyowl.legui.input.Mouse
import com.spinyowl.legui.style.color.ColorConstants

class ToggleButton(image: Image, action: String, toggled: Boolean):
      ImageButton(Vector2f(), image, action) {

    init {
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