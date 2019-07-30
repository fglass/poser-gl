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
      Dialog("Cache Loader", "Please backup your cache first", 260f, 136f)  {

    private lateinit var path: TextInput
    private val openIcon = BufferedImage(SPRITE_PATH + "open.png")
    private val openHoveredIcon = BufferedImage(SPRITE_PATH + "open-hovered.png")

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

        path = TextInput(76f, 35f, 97f, 15f)
        path.style.focusedStrokeColor = null
        container.add(path)

        val open = ImageButton(Vector2f(174f, 35f), openIcon)
        open.hoveredIcon = openHoveredIcon
        open.size = Vector2f(14f, 15f)

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

        val plugins = SelectBox<String>(76f, 63f, 112f, 15f)
        plugins.addElement("OSRS")
        plugins.addElement("317")
        plugins.addElement("Alternate 317")

        plugins.addSelectBoxChangeSelectionEventListener {
            // TODO
        }
        plugins.childComponents.forEach { it.style.focusedStrokeColor = null }
        container.add(plugins)
    }

    private fun addLoadButton() {
        val load = Button("Load", 107f, 90f, 45f, 15f)
        load.style.focusedStrokeColor = null

        load.listenerMap.addListener(MouseClickEvent::class.java) { event ->
            if (event.button == Mouse.MouseButton.MOUSE_BUTTON_LEFT &&
                event.action == MouseClickEvent.MouseClickAction.CLICK) {
                loadCache()
            }
        }
        container.add(load)
    }

    private fun loadCache() {
        context.cacheService.init(path.textState.text)

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