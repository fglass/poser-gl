package gui

import org.joml.Vector2f
import org.liquidengine.legui.component.ScrollablePanel
import org.liquidengine.legui.component.TextInput

abstract class ItemList(x: Float, y: Float, val gui: Gui): ScrollablePanel() {

    val listX = 2f
    val listY = 2f
    val listYOffset = 17

    init {
        position.x = x
        position.y = y
        size = getListSize()
        this.remove(horizontalScrollBar)
    }

    fun getListSize(): Vector2f {
        return Vector2f(150f, gui.size.y - 52)
    }

    fun adjustScroll(filteredSize: Int) {
        verticalScrollBar.curValue = 0f // Reset scroll position
        container.setSize(142f, listY + filteredSize * listYOffset) // Adjust scroll size
    }

    abstract fun search(searchField: TextInput)
}