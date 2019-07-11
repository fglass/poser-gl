package gui.component

import WIDTH
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.joml.Vector2f
import org.joml.Vector4f
import org.liquidengine.legui.component.ImageView
import org.liquidengine.legui.component.Tooltip
import org.liquidengine.legui.event.CursorEnterEvent
import org.liquidengine.legui.image.Image
import org.liquidengine.legui.style.Style
import org.liquidengine.legui.style.border.SimpleLineBorder
import org.liquidengine.legui.style.color.ColorConstants

open class ImageButton(position: Vector2f, private val icon: Image, action: String): ImageView(icon) {

    init {
        this.position = position
        size = Vector2f(image.width.toFloat(), image.height.toFloat())
        style.setBorderRadius(0f)
        style.border.isEnabled = false
        style.background.color = ColorConstants.transparent()

        if (action.isNotEmpty()) {
            addTooltip(action)
        }
    }

    private fun addTooltip(action: String) {
        tooltip = Tooltip(action)

        val colour = 35 / 255f
        tooltip.style.border = SimpleLineBorder(Vector4f(colour, colour, colour, 1f), 1f)

        tooltip.style.setBorderRadius(0f)
        tooltip.style.background.color = ColorConstants.darkGray()
        tooltip.textState.textColor = ColorConstants.white()
        tooltip.style.shadow = null

        val charWidth = if (action.length < 10) 8f else 6f
        val y = 15f
        tooltip.size = Vector2f(tooltip.textState.length() * charWidth, y)
        tooltip.position.y -= y

        listenerMap.addListener(CursorEnterEvent::class.java) { event ->
            if (event.isEntered) {
                tooltip.style.display = Style.DisplayType.NONE

                // Shift if off screen
                val offset = 6f
                val delta = tooltip.absolutePosition.x + tooltip.size.x + offset - WIDTH
                if (delta > 0) {
                    tooltip.position.x -= delta
                }

                // Delay displaying
                GlobalScope.launch {
                    delay(900)
                    tooltip.style.display = Style.DisplayType.FLEX
                }
            }
        }
    }

    fun addHover(hoveredImage: Image) {
        listenerMap.addListener(CursorEnterEvent::class.java) { event ->
            image = if (event.isEntered) hoveredImage else icon
        }
    }
}