package gui.component

import org.liquidengine.legui.component.Button
import org.liquidengine.legui.component.ScrollablePanel
import org.liquidengine.legui.event.MouseClickEvent
import org.liquidengine.legui.style.Style
import org.liquidengine.legui.style.color.ColorConstants
import util.Colour

abstract class ElementList: ScrollablePanel() { // TODO: refactor

    var searchText = "Search"
    var highlighted = emptyArray<Int>().toIntArray()
    protected val listX = 2f
    protected val listY = 3f
    protected val listYOffset = 18f
    protected val containerX = 163f

    init {
        style.setMargin(43f, 0f, 0f, 0f)
        style.position = Style.PositionType.RELATIVE
        style.setMaxWidth(173f)
        style.flexStyle.flexGrow = 1

        this.remove(horizontalScrollBar)
        style.background.color = Colour.GRAY.rgba

        style.border.isEnabled = false
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
                index < filtered.size -> {
                    val key = filtered[index]
                    element.value.highlighted = highlighted.contains(key)
                    handleElement(key, element.value) // Shift matches up
                }
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
            element.value.highlighted = false // Reset highlighting
            when {
                index < highlighted.size -> { // Only applies to animation list
                    element.value.highlighted = true
                    handleElement(highlighted[index], element.value)
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

    abstract class Element(x: Float, y: Float): Button(x, y, 160f, 15f) {

        init {
            style.background.color = ColorConstants.darkGray()
            style.border.isEnabled = false
        }

        var highlighted = false

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

