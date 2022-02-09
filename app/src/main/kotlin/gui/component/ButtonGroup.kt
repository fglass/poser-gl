package gui.component

import org.joml.Vector2f
import com.spinyowl.legui.component.Panel
import com.spinyowl.legui.image.Image
import util.setSizeLimits

open class ButtonGroup(position: Vector2f, private val iconSize: Vector2f, images: Array<Image> = emptyArray(),
                       actions: Array<String> = emptyArray(), private val padding: Int = 2) : Panel() { // TODO: redo

    val buttons = ArrayList<ImageButton>()
    private var containerX = padding + 1f
    private val offset = 3f

    init {
        repeat(images.size) {
            addButton(images[it], actions[it])
        }
        style.setMargin(position.y, 0f, 0f, position.x)
        style.border.isEnabled = false
        setSizes()
    }

    fun addButton(image: Image, action: String): ImageButton {
        val button = ImageButton(Vector2f(containerX, offset), image, action)
        button.size = iconSize
        containerX += button.size.x + padding
        button.style.setBorderRadius(1f)

        buttons.add(button)
        add(button)
        return button
    }

    fun setSizes() {
        setSizeLimits(containerX + 1, iconSize.y + 2 * offset)
        size.x = style.minWidth.get() as Float
        size.y = style.minHeight.get() as Float
    }
}