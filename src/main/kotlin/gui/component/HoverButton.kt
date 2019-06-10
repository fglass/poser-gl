package gui.component

import org.joml.Vector2f
import org.liquidengine.legui.event.CursorEnterEvent
import org.liquidengine.legui.image.Image

class HoverButton(position: Vector2f, image: Image, hoveredImage: Image):
    ImageButton(position, image) {

    init {
        listenerMap.addListener(CursorEnterEvent::class.java) { event ->
            this.image = if (event.isEntered) hoveredImage else image
        }
    }
}