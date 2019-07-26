package gui.panel

import Processor
import gui.component.AnimationList
import gui.component.EntityList
import gui.component.ItemList
import org.liquidengine.legui.component.Button
import org.liquidengine.legui.component.Label
import org.liquidengine.legui.component.Panel
import org.liquidengine.legui.component.TextInput
import org.liquidengine.legui.component.optional.align.HorizontalAlign
import org.liquidengine.legui.event.KeyEvent
import org.liquidengine.legui.event.MouseClickEvent
import org.liquidengine.legui.style.Style
import org.liquidengine.legui.style.color.ColorConstants
import org.liquidengine.legui.style.flex.FlexStyle
import org.lwjgl.glfw.GLFW
import util.setSizeLimits

class ListPanel(context: Processor): Panel() {

    private val search = TextInput("Search")
    private val tabs = LinkedHashSet<Button>()
    private val entityList = EntityList(context)
    val animationList = AnimationList(context)
    private val itemList = ItemList(context)
    private val lists = arrayOf(entityList, animationList, itemList)

    init {
        style.display = Style.DisplayType.FLEX
        style.flexStyle.flexDirection = FlexStyle.FlexDirection.COLUMN

        style.position = Style.PositionType.RELATIVE
        style.setMaxWidth(174f)
        style.flexStyle.flexGrow = 1
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

        search.setSizeLimits(164f, 15f)
        search.style.setMargin(5f, 0f, 0f, 5f)
        search.style.focusedStrokeColor = null
        add(search)
    }

    private fun addTabs() {
        val names = arrayOf("Entity", "Seq", "Item")
        val offset = 55f

        for ((i, name) in names.withIndex()) {
            val tab = Button("")
            tab.setSizeLimits(offset - 1, 18f)
            tab.style.setMargin(26f, 0f, 0f, 5f + i * offset)
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

            val label = Label(name) // For more flexible aligning
            label.setSizeLimits(30f, 15f)
            label.style.setMargin(26f, 0f, 0f, 17f + i * offset)
            label.textState.horizontalAlign = HorizontalAlign.CENTER
            label.isFocusable = false
            add(label)
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
}