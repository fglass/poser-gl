package gui.panel

import gui.component.AnimationList
import gui.component.NpcList
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
    private val npcList = NpcList(5f, 49f, gui, context)
    private val animationList = AnimationList(5f, 49f, gui, context)

    init {
        position = Vector2f(0f, 0f)
        size = getPanelSize()
        style.border.isEnabled = false

        addSearch()
        addSelectBox()
        add(npcList)
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
                val input = search.textState.text
                if (contains(npcList)) npcList.search(input) else animationList.search(input)
            }
        }
        search.style.focusedStrokeColor = null
        add(search)
    }

    private fun addSelectBox() {
        val selectBox = SelectBox<String>(5f, 27f, 150f, 15f)
        selectBox.addElement("NPCs")
        selectBox.addElement("Animations")

        selectBox.addSelectBoxChangeSelectionEventListener { event ->
            when (event.newValue.toString()) {
                "NPCs" -> {
                    if (contains(animationList)) {
                        animationList.searchText = search.textState.text
                        setSearchText(npcList.searchText)
                        remove(animationList)
                        add(npcList)
                    }
                }
                "Animations" -> {
                    if (contains(npcList)) {
                        npcList.searchText = search.textState.text
                        setSearchText(animationList.searchText)
                        remove(npcList)
                        add(animationList)
                    }
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
        npcList.resize()
        animationList.resize()
    }

    private fun getPanelSize(): Vector2f {
        return Vector2f(156f, gui.size.y)
    }
}