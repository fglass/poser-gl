package gui.component

import gui.panel.AnimationPanel
import org.liquidengine.legui.component.Panel
import org.liquidengine.legui.component.event.component.ChangeSizeEvent
import org.liquidengine.legui.event.MouseClickEvent
import org.liquidengine.legui.style.Style
import util.Colour
import util.setHeightLimit

class AnimationTimeline(private val parent: AnimationPanel): Panel() {
    init {
        style.position = Style.PositionType.RELATIVE
        style.setMargin(4f, 14f, 0f, 14f)
        setHeightLimit(65f)
        style.flexStyle.flexGrow = 1

        style.background.color = Colour.GRAY.rgba
        style.focusedStrokeColor = null
        style.setBorderRadius(0f)

        listenerMap.addListener(ChangeSizeEvent::class.java) { _ ->
            parent.setTimeline()
        }

        listenerMap.addListener(MouseClickEvent::class.java) { event ->
            if (event.action == MouseClickEvent.MouseClickAction.CLICK) {
                parent.adjustTime(event.position.x)
            }
        }
    }

    fun getUnitX(maxLength: Int): Float {
        return size.x / maxLength
    }
}