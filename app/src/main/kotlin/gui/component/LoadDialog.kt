package gui.component

import SPRITE_PATH
import org.joml.Vector2f
import org.liquidengine.legui.component.*
import org.liquidengine.legui.component.optional.align.HorizontalAlign
import org.liquidengine.legui.image.BufferedImage

class LoadDialog: Dialog("Cache Loader", "Please backup your cache first", 260f, 118f)  {

    private val openIcon = BufferedImage(SPRITE_PATH + "open.png")
    private val openHoveredIcon = BufferedImage(SPRITE_PATH + "open-hovered.png")

    init {
        isDraggable = false
        isCloseable = false
        container.remove(message)

        val backup = Label("Please backup your cache first", 0f, 8f, size.x, 15f)
        backup.textState.horizontalAlign = HorizontalAlign.CENTER
        container.add(backup)

        addPath()
        addPlugin()
    }

    private fun addPath() {
        val pathLabel = Label("Path:", 9f, 35f, 40f, 15f)
        pathLabel.textState.horizontalAlign = HorizontalAlign.RIGHT
        container.add(pathLabel)

        val box = Panel(172f, 35f, 16f, 15f)
        container.add(box)

        val path = TextInput(76f, 35f, 97f, 15f)
        path.style.focusedStrokeColor = null
        container.add(path)

        val open = ImageButton(Vector2f(174f, 35f), openIcon, "Open")
        open.hoveredIcon = openHoveredIcon
        open.size = Vector2f(14f, 15f)
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
}