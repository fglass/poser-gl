package gui.component

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.joml.Vector2f
import org.liquidengine.legui.component.ImageView
import org.liquidengine.legui.component.Tooltip
import org.liquidengine.legui.event.CursorEnterEvent
import org.liquidengine.legui.image.Image
import org.liquidengine.legui.style.Style
import org.liquidengine.legui.style.border.SimpleLineBorder
import org.liquidengine.legui.style.color.ColorConstants
import org.liquidengine.legui.style.color.ColorUtil

open class ImageButton(position: Vector2f, private var icon: Image, action: String = ""): ImageView(icon) {

    var hoveredIcon: Image? = null

    init {
        this.position = position
        size = Vector2f(image.width.toFloat(), image.height.toFloat())
        style.setBorderRadius(0f)
        style.border.isEnabled = false
        style.background.color = ColorConstants.transparent()

        if (action.isNotEmpty()) {
            addTooltip(action)
        }

        listenerMap.addListener(CursorEnterEvent::class.java) { event ->
            image = if (event.isEntered && hoveredIcon != null) hoveredIcon else icon
        }
    }

    fun setIconImage(icon: Image) {
        this.icon = icon
        image = icon
    }

    private fun addTooltip(action: String) {
        tooltip = Tooltip(action)
        tooltip.style.setBorderRadius(0f)
        tooltip.style.border = SimpleLineBorder(ColorUtil.fromInt(35, 35, 35, 1f), 1f)
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
                shiftTooltip()
                GlobalScope.launch {
                    delay(900) // Delay displaying
                    tooltip.style.display = Style.DisplayType.MANUAL
                }
            }
        }
    }

    private fun shiftTooltip() {
        var parent = tooltip.parent
        while (parent.parent != null) {
            parent = parent.parent
        }

        val frameWidth = parent.size.x
        val offset = 6f

        val delta = tooltip.absolutePosition.x + tooltip.size.x + offset - frameWidth // Shift if off screen
        if (delta > 0) {
            tooltip.position.x -= delta
        }
    }
}