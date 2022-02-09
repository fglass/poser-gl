package gui.panel

import render.RenderContext
import gui.component.AnimationList
import gui.component.EntityList
import gui.component.ItemList
import com.spinyowl.legui.component.Button
import com.spinyowl.legui.component.Label
import com.spinyowl.legui.component.Panel
import com.spinyowl.legui.component.TextInput
import com.spinyowl.legui.component.optional.align.HorizontalAlign
import com.spinyowl.legui.event.FocusEvent
import com.spinyowl.legui.event.KeyEvent
import com.spinyowl.legui.event.MouseClickEvent
import com.spinyowl.legui.style.Style
import com.spinyowl.legui.style.color.ColorConstants
import com.spinyowl.legui.style.flex.FlexStyle
import org.lwjgl.glfw.GLFW
import util.setSizeLimits

class ListPanel(context: RenderContext): Panel() {

    private val search = TextInput()
    private val tabs = LinkedHashSet<Button>()
    private val entityList = EntityList(context)
    val animationList = AnimationList(context)
    private val itemList = ItemList(context)
    private val lists = arrayOf(entityList, animationList, itemList)

    init {
        style.display = Style.DisplayType.FLEX
        style.flexStyle.flexDirection = FlexStyle.FlexDirection.COLUMN

        style.position = Style.PositionType.RELATIVE
        style.setMaxWidth(173f)
        style.flexStyle.flexGrow = 1
        style.background.color = ColorConstants.darkGray()
        style.focusedStrokeColor = null
        style.setMargin(5f, 5f, 5f, 5f)

        addSearch()
        addTabs()
        add(entityList)
    }

    private fun addSearch() {
        setSearchPlaceholder()
        search.listenerMap.addListener(FocusEvent::class.java) { event ->
            if (event.isFocused && search.textState.text == "Search") {
                resetSearchPlaceholder()
            } else if (search.textState.text.isEmpty()) {
                setSearchPlaceholder()
            }
        }
        search.listenerMap.addListener(KeyEvent::class.java) { event ->
            if (event.action == GLFW.GLFW_RELEASE) {
                lists.first(::contains).search(search.textState.text)
            }
        }

        search.setSizeLimits(164f, 15f)
        search.style.setMargin(5f, 0f, 0f, 5f)
        search.style.focusedStrokeColor = null
        add(search)
    }

    private fun setSearchPlaceholder() {
        search.textState.text = "Search"
        search.style.textColor = ColorConstants.gray()
    }

    private fun resetSearchPlaceholder() {
        search.textState.text = ""
        search.style.textColor = ColorConstants.white()
    }

    private fun addTabs() {
        val names = arrayOf("Entity", "Seq", "Item")
        val offset = 58f

        for ((i, name) in names.withIndex()) {
            val tab = Button("")
            tab.setSizeLimits(offset - 1, 18f)
            tab.style.setMargin(26f, 0f, 0f, i * offset)
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
            label.style.setMargin(26f, 0f, 0f, 12f + i * offset)
            label.style.horizontalAlign = HorizontalAlign.CENTER
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
        search.style.textColor = if (text == "Search") ColorConstants.gray() else ColorConstants.white()
        search.textState.text = text
    }
}