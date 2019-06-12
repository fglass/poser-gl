package gui.component

import gui.Gui
import org.joml.Vector2f
import org.liquidengine.legui.component.Button
import org.liquidengine.legui.component.ScrollablePanel
import org.liquidengine.legui.event.MouseClickEvent
import org.liquidengine.legui.style.color.ColorConstants

abstract class ItemList(x: Float, y: Float, val gui: Gui): ScrollablePanel() {

    var searchText = "Search"
    protected var maxIndex = 0
    protected val listX = 2f
    protected val listY = 2f
    protected val listYOffset = 17

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
        return Vector2f(150f, gui.size.y - 159)
    }

    fun search(input: String) {
        val filtered = getFiltered(input)
        val items = getItems()
        adjustScroll(filtered.size)

        for (i in 0 until items.size) {
            val item = items[i]
            when {
                filtered.size >= maxIndex -> handleItem(i, item) // Reset as no matches
                i < filtered.size -> handleItem(filtered[i], item) // Shift matches up
                else -> item.isEnabled = false // Hide filtered
            }
        }
    }

    abstract fun getFiltered(input: String): List<Int>

    abstract fun getItems(): List<Item>

    abstract fun handleItem(index: Int, item: Item)

    private fun adjustScroll(filteredSize: Int) {
        verticalScrollBar.curValue = 0f // Reset scroll position
        container.setSize(142f, listY + filteredSize * listYOffset) // Adjust scroll size
    }

    abstract class Item(x: Float, y: Float, width: Float, height: Float): Button(x, y, width, height) {

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

