package gui.component

import SPRITE_PATH
import Processor
import gui.GuiManager
import org.joml.Vector2f
import org.liquidengine.legui.component.*
import org.liquidengine.legui.component.optional.align.HorizontalAlign
import org.liquidengine.legui.event.MouseClickEvent
import org.liquidengine.legui.image.BufferedImage
import org.liquidengine.legui.input.Mouse
import org.liquidengine.legui.style.color.ColorConstants
import util.FileDialogs

class LoadDialog(private val context: Processor):
      Dialog("Cache Loader", "Please backup your cache first", 260f, 109f)  {

    private lateinit var path: TextInput
    private lateinit var plugins: SelectBox<String>
    private val openIcon = BufferedImage(SPRITE_PATH + "open.png")
    private val openHoveredIcon = BufferedImage(SPRITE_PATH + "open-hovered.png")
    private val loadIcon = BufferedImage(SPRITE_PATH + "load.png")
    private val loadHoveredIcon = BufferedImage(SPRITE_PATH + "load-hovered.png")

    init {
        isDraggable = false
        isCloseable = false
        message.position.y -= 7f

        addPath()
        addPlugin()
        addLoadButton()
    }

    private fun addPath() {
        val pathLabel = Label("Path:", 9f, 35f, 40f, 15f)
        pathLabel.textState.horizontalAlign = HorizontalAlign.RIGHT
        container.add(pathLabel)

        val box = Panel(172f, 35f, 16f, 15f)
        container.add(box)

        path = TextInput("/Users/fred/Documents/PoserGL/repository/cache", 76f, 35f, 97f, 15f) // TODO: remove placeholder
        path.style.focusedStrokeColor = null
        container.add(path)

        val open = ImageButton(Vector2f(175f, 35f), openIcon)
        open.hoveredIcon = openHoveredIcon
        open.size = Vector2f(13f, 14f)

        open.listenerMap.addListener(MouseClickEvent::class.java) { event ->
            if (event.button == Mouse.MouseButton.MOUSE_BUTTON_LEFT &&
                event.action == MouseClickEvent.MouseClickAction.CLICK) {
                path.textState.text = FileDialogs.openFile(listOf(), ".", true)?: return@addListener
            }
        }

        container.add(open)
    }

    private fun addPlugin() {
        val pluginLabel = Label("Plugin:", 9f, 63f, 40f, 15f)
        pluginLabel.textState.horizontalAlign = HorizontalAlign.RIGHT
        container.add(pluginLabel)

        plugins = SelectBox(76f, 63f, 112f, 15f)
        plugins.addElement("OSRS")
        plugins.addElement("317")
        plugins.addElement("Legacy 317")
        plugins.childComponents.forEach { it.style.focusedStrokeColor = null }
        container.add(plugins)
    }

    private fun addLoadButton() {
        val load = ImageButton(Vector2f(235f, 63f), loadIcon)
        load.hoveredIcon = loadHoveredIcon
        load.size = Vector2f(16f, 16f)

        load.listenerMap.addListener(MouseClickEvent::class.java) { event ->
            if (event.button == Mouse.MouseButton.MOUSE_BUTTON_LEFT &&
                event.action == MouseClickEvent.MouseClickAction.CLICK) {
                loadCache()
            }
        }
        container.add(load)
    }

    private fun loadCache() {
        if (path.textState.text.isEmpty()) {
            message.textState.text = "Please select a cache first"
            return
        }

        // Try to load selected cache
        context.cacheService.init(path.textState.text, plugins.selection)

        if (context.cacheService.loaded) {
            context.gui = GuiManager(context)
            context.entityHandler.loadPlayer()
            close()
        } else {
            message.textState.text = "Unable to load a valid cache"
            message.textState.textColor = ColorConstants.lightRed()
        }
    }
}