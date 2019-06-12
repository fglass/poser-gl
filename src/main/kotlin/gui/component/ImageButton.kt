package gui.component

import org.joml.Vector2f
import org.liquidengine.legui.component.ImageView
import org.liquidengine.legui.image.Image
import org.liquidengine.legui.style.color.ColorConstants

open class ImageButton(position: Vector2f, image: Image): ImageView(image) {

    init {
        this.position = position
        size = Vector2f(image.width.toFloat(), image.height.toFloat())
        style.setBorderRadius(0f)
        style.border.isEnabled = false
        style.background.color = ColorConstants.transparent()
    }
}