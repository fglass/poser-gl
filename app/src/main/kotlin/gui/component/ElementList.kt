package gui.component

import BG_COLOUR
import gui.GuiManager
import org.joml.Vector2f
import org.liquidengine.legui.component.Button
import org.liquidengine.legui.component.ScrollablePanel
import org.liquidengine.legui.event.MouseClickEvent
import org.liquidengine.legui.style.Style
import org.liquidengine.legui.style.color.ColorConstants
import util.setSizeLimits

abstract class ElementList: ScrollablePanel() {

    var searchText = "Search"
    protected val listX = 2f
    protected val listY = 3f
    protected val listYOffset = 18f
    protected val containerX = 157f

    init {
        style.setMargin(43f, 0f, 5f, 5f)
        style.position = Style.PositionType.RELATIVE
        style.setMaxWidth(164f)
        style.flexStyle.flexGrow = 1

        this.remove(horizontalScrollBar)
        style.background.color = BG_COLOUR

        container.style.border.isEnabled = false
        viewport.style.border.isEnabled = false
        viewport.style.setBottom(0f)

        verticalScrollBar.style.setBottom(0f)
        verticalScrollBar.scrollStep = 0.3f
        verticalScrollBar.style.focusedStrokeColor = null

        // Click to move proportionally
        verticalScrollBar.listenerMap.addListener(MouseClickEvent::class.java) { event ->
            if (event.action == MouseClickEvent.MouseClickAction.CLICK) {
                val offset = event.position.y / size.y
                verticalScrollBar.curValue = verticalScrollBar.maxValue * offset
            }
        }
    }

    fun search(input: String) {
        val filtered = getFiltered(input)
        val elements = getElements()
        adjustScroll(filtered.size)

        var index = 0
        for (element in elements) {
            when {
                filtered.size >= elements.size -> handleElement(element.key, element.value) // Reset as no matches
                index < filtered.size -> handleElement(filtered[index], element.value) // Shift matches up
                else -> element.value.isEnabled = false // Hide filtered
            }
            index++
        }
    }

    abstract fun getFiltered(input: String): List<Int>

    abstract fun getElements(): HashMap<Int, Element>

    abstract fun handleElement(index: Int, element: Element)

    private fun adjustScroll(filteredSize: Int) {
        verticalScrollBar.curValue = 0f // Reset scroll position
        container.size.y = listY + filteredSize * listYOffset // Adjust scroll size
    }

    abstract class Element(x: Float, y: Float, width: Float, height: Float): Button(x, y, width, height) {

        init {
            style.background.color = ColorConstants.darkGray()
            style.border.isEnabled = false
        }

        abstract fun updateText()

        fun addClickListener() {
            listenerMap.addListener(MouseClickEvent::class.java) { event ->
                if (event.action == MouseClickEvent.MouseClickAction.CLICK) {
                    onClickEvent()
                }
            }
        }

        abstract fun onClickEvent()
    }
}

