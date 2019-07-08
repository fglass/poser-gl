package gui.panel

import Processor
import gui.GuiManager
import gui.component.AnimationList
import gui.component.EntityList
import gui.component.ItemList
import org.joml.Vector2f
import org.liquidengine.legui.component.Button
import org.liquidengine.legui.component.Panel
import org.liquidengine.legui.component.TextInput
import org.liquidengine.legui.event.KeyEvent
import org.liquidengine.legui.event.MouseClickEvent
import org.liquidengine.legui.style.color.ColorConstants
import org.lwjgl.glfw.GLFW

class ListPanel(private val gui: GuiManager, context: Processor): Panel() {

    private val search = TextInput("Search", 5f, 5f, 164f, 15f)
    private val tabs = LinkedHashSet<Button>()
    private val entityList = EntityList(5f, 43f, gui, context)
    val animationList = AnimationList(5f, 43f, gui, context)
    private val itemList = ItemList(5f, 43f, gui, context)
    private val lists = arrayOf(entityList, animationList, itemList)

    init {
        position = Vector2f(0f, 0f)
        size = getPanelSize()
        style.border.isEnabled = false

        addSearch()
        addTabs()
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

    private fun addTabs() {
        val names = arrayOf("Entity", "Seq", "Item")
        val offset = 55f

        for ((i, name) in names.withIndex()) {
            val tab = Button(name, 5f + i * offset, 27f, offset - 1, 17f)
            tab.style.focusedStrokeColor = null
            tab.style.setBorderRadius(1f)

            tab.listenerMap.addListener(MouseClickEvent::class.java) { event ->
                if (event.action == MouseClickEvent.MouseClickAction.CLICK) {
                    changeList(tab)
                    selectTab(tab)
                }
            }
            tabs.add(tab)
            add(tab)
        }
        selectTab(tabs.first())
    }

    private fun changeList(tab: Button) {
        val list = lists.first { contains(it) }
        list.searchText = search.textState.text
        remove(list)

        val newList = lists[tabs.indexOf(tab)]
        setSearchText(newList.searchText)
        add(newList)
    }

    private fun selectTab(tab: Button) {
        tabs.forEach { it.style.background.color =
            if (it == tab) it.hoveredStyle.background.color else it.focusedStyle.background.color
        }
    }

    private fun setSearchText(text: String) {
        search.textState.textColor = if (text == "Search") ColorConstants.gray() else ColorConstants.white()
        search.textState.text = text
    }

    fun resize() {
        size = getPanelSize()
        lists.forEach { it.resize() }
    }

    private fun getPanelSize(): Vector2f {
        return Vector2f(170f, gui.size.y - 100)
    }
}