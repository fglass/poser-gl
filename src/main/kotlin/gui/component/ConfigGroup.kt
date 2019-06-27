package gui.component

import org.joml.Vector2f
import org.liquidengine.legui.component.Panel
import org.liquidengine.legui.event.MouseClickEvent
import org.liquidengine.legui.image.Image
import org.liquidengine.legui.input.Mouse
import org.liquidengine.legui.style.color.ColorConstants

class ConfigGroup(x: Float, y: Float, vararg images: Image): Panel() {

    val buttons = ArrayList<ImageButton>()

    init {
        val iconSize = Vector2f(24f, 24f)
        var containerX = 3f
        val offset = 3f

        for (image in images) {
            val button = ImageButton(Vector2f(containerX, offset), image)
            button.size = iconSize
            containerX += button.size.x + 2

            if (image == images[0]) {
                button.style.background.color = ColorConstants.gray()
            }

            button.style.setBorderRadius(1f)
            button.listenerMap.addListener(MouseClickEvent::class.java) { event ->
                if (event.button == Mouse.MouseButton.MOUSE_BUTTON_LEFT &&
                    event.action == MouseClickEvent.MouseClickAction.CLICK) {
                    updateConfigs(button)
                }
            }

            buttons.add(button)
            add(button)
        }

        position = Vector2f(x, y)
        size = Vector2f(containerX + 1, iconSize.y + 2 * offset)
        style.border.isEnabled = false
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