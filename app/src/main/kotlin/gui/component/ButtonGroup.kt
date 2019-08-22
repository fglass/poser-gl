package gui.component

import org.joml.Vector2f
import org.liquidengine.legui.component.Panel
import org.liquidengine.legui.image.Image
import util.setSizeLimits

open class ButtonGroup(position: Vector2f, iconSize: Vector2f, images: Array<Image>, actions: Array<String>): Panel() {

    val buttons = ArrayList<ImageButton>()

    init {
        var containerX = 3f
        val offset = 3f

        repeat(images.size) {
            val button = ImageButton(Vector2f(containerX, offset), images[it], actions[it])
            button.size = iconSize
            containerX += button.size.x + 2

            button.style.setBorderRadius(1f)
            buttons.add(button)
            this.add(button)
        }

        setSizeLimits(containerX + 1, iconSize.y + 2 * offset)
        size.x = style.minWidth.get() as Float
        size.y = style.minHeight.get() as Float
        style.setMargin(position.y, 0f, 0f, position.x)
        //style.border.isEnabled = false
    }
}