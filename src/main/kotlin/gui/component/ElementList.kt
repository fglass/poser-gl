package gui.component

import gui.GuiManager
import org.joml.Vector2f
import org.liquidengine.legui.component.Button
import org.liquidengine.legui.component.ScrollablePanel
import org.liquidengine.legui.event.MouseClickEvent
import org.liquidengine.legui.style.color.ColorConstants

abstract class ElementList(x: Float, y: Float, val gui: GuiManager): ScrollablePanel() {

    var searchText = "Search"
    protected var maxIndex = 0
    protected val listX = 2f
    protected val listY = 2f
    protected val listYOffset = 17
    protected val containerX = 157f

    init {
        position.x = x
        position.y = y
        size = getListSize()
        this.remove(horizontalScrollBar)
    }

    fun resize() {
        size = getListSize()
    }

    private fun getListSize(): Vector2f {
        return Vector2f(164f, gui.size.y - 166)
    }

    fun search(input: String) {
        val filtered = getFiltered(input)
        val elements = getElements()
        adjustScroll(filtered.size)

        for (i in 0 until elements.size) {
            val element = elements[i]
            when {
                filtered.size >= maxIndex -> handleElement(i, element) // Reset as no matches
                i < filtered.size -> handleElement(filtered[i], element) // Shift matches up
                else -> element.isEnabled = false // Hide filtered
            }
        }
    }

    abstract fun getFiltered(input: String): List<Int>

    abstract fun getElements(): List<Element>

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

