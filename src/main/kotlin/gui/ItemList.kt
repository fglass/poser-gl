package gui

import org.joml.Vector2f
import org.liquidengine.legui.component.ScrollablePanel

abstract class ItemList(x: Float, y: Float, val gui: Gui): ScrollablePanel() {

    var maxIndex = 0
    val listX = 2f
    val listY = 2f
    val listYOffset = 17

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
        return Vector2f(150f, gui.size.y - 52)
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

    fun resetSearch() {
        search("")
    }
}