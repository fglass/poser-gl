package gui.component

import org.joml.Vector2f
import org.liquidengine.legui.component.Panel
import org.liquidengine.legui.image.Image

open class ButtonGroup(position: Vector2f, iconSize: Vector2f, vararg images: Image): Panel() {

    val buttons = ArrayList<ImageButton>()

    init {
        var containerX = 3f
        val offset = 3f

        for (image in images) {
            val button = ImageButton(Vector2f(containerX, offset), image)
            button.size = iconSize
            containerX += button.size.x + 2

            button.style.setBorderRadius(1f)
            buttons.add(button)
            this.add(button)
        }

        this.position = position
        size = Vector2f(containerX + 1, iconSize.y + 2 * offset)
        style.border.isEnabled = false
    }
}