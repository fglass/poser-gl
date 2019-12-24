package gui.component

import api.cache.ICacheLoader
import gui.GuiManager
import render.VERSION
import org.joml.Vector2f
import org.liquidengine.legui.component.*
import org.liquidengine.legui.event.MouseClickEvent
import org.liquidengine.legui.input.Mouse
import org.liquidengine.legui.style.color.ColorConstants
import org.liquidengine.legui.style.color.ColorUtil
import render.RenderContext
import util.FileDialog
import util.ResourceMap

class StartDialog(private val context: RenderContext):
      Dialog("", "Backup your cache before making changes", context, 260f, 177f) {

    private lateinit var cache: TextInput
    private lateinit var plugins: SelectBox<ICacheLoader>

    init {
        isDraggable = false
        isCloseable = false

        remove(titleContainer)
        remove(message)
        container.style.background.color = ColorUtil.fromInt(35, 35, 35, 1f)
        message.position.y += 77f

        addTitle()
        addPath()
        addPlugins()
        addLoadButton()
    }

    private fun addTitle() {
        val logo = ImageView(ResourceMap["title"])
        logo.position = Vector2f(20f, 5f)
        logo.size = Vector2f(220f, 82f)
        logo.style.border.isEnabled = false
        logo.style.background.color = ColorConstants.transparent()
        container.add(logo)

        val version = Label("v$VERSION")
        version.position = Vector2f(208f, 66f)
        container.add(version)
    }

    private fun addPath() {
        val cacheLabel = Label("Cache:", 14f, 122f, 50f, 15f)
        container.add(cacheLabel)

        val box = Panel(172f, 122f, 16f, 15f)
        box.style.focusedStrokeColor = null
        container.add(box)

        cache = TextInput(76f, 122f, 97f, 15f)
        cache.style.focusedStrokeColor = null
        container.add(cache)

        val open = ImageButton(Vector2f(175f, 122f), ResourceMap["open"])
        open.hoveredIcon = ResourceMap["open-hovered"]
        open.size = Vector2f(13f, 14f)

        open.listenerMap.addListener(MouseClickEvent::class.java) { event ->
            if (event.button == Mouse.MouseButton.MOUSE_BUTTON_LEFT &&
                event.action == MouseClickEvent.MouseClickAction.CLICK) {
                cache.textState.text = FileDialog.openFile(folder = true)
            }
        }
        container.add(open)
    }

    private fun addPlugins() {
        val pluginLabel = Label("Plugin:", 14f, 150f, 50f, 15f)
        container.add(pluginLabel)

        plugins = SelectBox(76f, 150f, 112f, 15f)
        context.loaders.forEach {
            plugins.addElement(it)
        }
        plugins.expandButton.style.border.isEnabled = false
        plugins.childComponents.forEach { it.style.focusedStrokeColor = null }
        plugins.visibleCount = 4
        container.add(plugins)
    }

    private fun addLoadButton() {
        val load = ImageButton(Vector2f(232f, 150f), ResourceMap["load"])
        load.hoveredIcon = ResourceMap["load-hovered"]
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
        if (cache.textState.text.isEmpty()) {
            message.textState.text = "Select a cache first"
            return
        }

        // Try to load selected cache
        context.cacheService.init(cache.textState.text, plugins.selection)

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