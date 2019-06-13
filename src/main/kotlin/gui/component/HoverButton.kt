package gui.component

import org.joml.Vector2f
import org.liquidengine.legui.component.Panel
import org.liquidengine.legui.event.CursorEnterEvent
import org.liquidengine.legui.image.Image

class HoverButton(position: Vector2f, border: Float, image: Image, hoveredImage: Image): Panel() {

    init {
        this.position = Vector2f(position.x - border, position.y)
        this.size = Vector2f(image.width + 2 * border, image.height.toFloat())
        style.border.isEnabled = false
        style.focusedStrokeColor = null

        val imageView = ImageButton(position.sub(this.position), image)
        listenerMap.addListener(CursorEnterEvent::class.java) { event ->
            imageView.image = if (event.isEntered) hoveredImage else image
        }
        add(imageView)
    }
}