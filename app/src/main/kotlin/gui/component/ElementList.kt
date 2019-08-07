package gui.component

import BG_COLOUR
import org.liquidengine.legui.component.Button
import org.liquidengine.legui.component.ScrollablePanel
import org.liquidengine.legui.event.MouseClickEvent
import org.liquidengine.legui.style.Style
import org.liquidengine.legui.style.color.ColorConstants

abstract class ElementList: ScrollablePanel() {

    var searchText = "Search"
    var highlighted = emptyArray<Int>().toIntArray()
    protected val listX = 2f
    protected val listY = 3f
    protected val listYOffset = 18f
    protected val containerX = 156f

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

        if (filtered.size >= elements.size) { // No matches
            reset()
            return
        }

        var index = 0
        for (element in elements) {
            when {
                index < filtered.size -> handleElement(filtered[index], element.value) // Shift matches up
                else -> element.value.isEnabled = false // Hide filtered
            }
            index++
        }
    }

    fun reset() {
        var index = 0
        val elements = getElements()
        val start = if (this is EntityList) -1 else 0 // Account for player
        val regular = (start..elements.size).filter { !highlighted.contains(it) } // Exclude any highlighted elements

        for (element in elements) {
            when {
                this is AnimationList && index < highlighted.size -> { // Only applies to animation list
                    handleElement(highlighted[index], element.value)
                    (element.value as AnimationList.AnimationElement).highlight()
                }
                else -> handleElement(regular[index - highlighted.size], element.value)
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

    abstract class Element(x: Float, y: Float): Button(x, y, 151f, 15f) {

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

