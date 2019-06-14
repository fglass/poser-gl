package gui.panel

import gui.component.AnimationList
import gui.component.EntityList
import gui.component.ItemList
import org.liquidengine.legui.component.Panel
import org.liquidengine.legui.component.SelectBox
import org.liquidengine.legui.component.TextInput
import org.liquidengine.legui.event.KeyEvent
import org.liquidengine.legui.event.MouseClickEvent
import org.liquidengine.legui.style.color.ColorConstants
import org.lwjgl.glfw.GLFW
import Processor
import gui.Gui
import org.joml.Vector2f

class ListPanel(private val gui: Gui, context: Processor): Panel() {

    private val search = TextInput("Search", 5f, 5f, 150f, 15f)
    private val entityList = EntityList(5f, 49f, gui, context)
    private val animationList = AnimationList(5f, 49f, gui, context)
    private val itemList = ItemList(5f, 49f, gui, context)
    val lists = arrayOf(entityList, animationList, itemList)

    init {
        position = Vector2f(0f, 0f)
        size = getPanelSize()
        style.border.isEnabled = false

        addSearch()
        addSelectBox()
        add(entityList)
    }

    private fun addSearch() {
        search.textState.textColor = ColorConstants.gray()
        search.listenerMap.addListener(MouseClickEvent::class.java) {
            if (search.textState.text == "Search") { // Placeholder text
                search.textState.text = ""
                search.textState.textColor = ColorConstants.white()
            }
        }
        search.listenerMap.addListener(KeyEvent::class.java) { event ->
            if (event.action == GLFW.GLFW_RELEASE) {
                lists.first { contains(it) }.search(search.textState.text)
            }
        }
        search.style.focusedStrokeColor = null
        add(search)
    }

    private fun addSelectBox() {
        val selectBox = SelectBox<String>(5f, 27f, 150f, 15f)
        selectBox.addElement("Entity")
        selectBox.addElement("Animation")
        selectBox.addElement("Item")

        selectBox.addSelectBoxChangeSelectionEventListener { event ->
            val list = lists.first { contains(it) }
            list.searchText = search.textState.text
            remove(list)
            when (event.newValue.toString()) {
                "Entity" -> {
                    setSearchText(entityList.searchText)
                    add(entityList)
                }
                "Animation" -> {
                    setSearchText(animationList.searchText)
                    add(animationList)
                }
                "Item" -> {
                    setSearchText(itemList.searchText)
                    add(itemList)
                }
            }
        }
        add(selectBox)
    }

    private fun setSearchText(text: String) {
        search.textState.textColor = if (text == "Search") ColorConstants.gray() else ColorConstants.white()
        search.textState.text = text
    }

    fun resize() {
        size = getPanelSize()
        entityList.resize()
        animationList.resize()
    }

    private fun getPanelSize(): Vector2f {
        return Vector2f(156f, gui.size.y)
    }
}