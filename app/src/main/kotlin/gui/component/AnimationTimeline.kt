package gui.component

import render.BG_COLOUR
import gui.panel.AnimationPanel
import org.liquidengine.legui.component.Panel
import org.liquidengine.legui.event.MouseClickEvent
import org.liquidengine.legui.style.Style
import util.setHeightLimit

class AnimationTimeline(private val parent: AnimationPanel): Panel() {

    init {
        style.position = Style.PositionType.RELATIVE
        style.setMargin(4f, 14f, 0f, 14f)
        setHeightLimit(65f)
        style.flexStyle.flexGrow = 1

        style.background.color = BG_COLOUR
        style.focusedStrokeColor = null
        style.setBorderRadius(0f)

        listenerMap.addListener(MouseClickEvent::class.java) { event ->
            if (event.action == MouseClickEvent.MouseClickAction.CLICK) {
                parent.adjustTime(event.position.x)
            }
        }
    }

    override fun setSize(width: Float, height: Float) {
        val previous = size.x
        super.setSize(width, height)
        if (previous != width) {
            parent.setTimeline()
        }
    }

    fun getUnitX(maxLength: Int): Float {
        return size.x / maxLength
    }
}