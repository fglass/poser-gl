package gui.panel

import render.RenderContext
import entity.Entity
import gui.component.ConfigGroup
import org.joml.Vector2f
import org.liquidengine.legui.component.ImageView
import org.liquidengine.legui.component.Label
import org.liquidengine.legui.component.Panel
import org.liquidengine.legui.component.ScrollablePanel
import org.liquidengine.legui.component.optional.align.HorizontalAlign
import org.liquidengine.legui.event.MouseClickEvent
import org.liquidengine.legui.input.Mouse
import org.liquidengine.legui.style.Style
import org.liquidengine.legui.style.color.ColorConstants
import org.liquidengine.legui.style.color.ColorUtil
import org.liquidengine.legui.style.flex.FlexStyle
import render.PolygonMode
import shader.ShadingType
import util.Colour
import util.ResourceMap
import util.setSizeLimits

class ManagerPanel(private val context: RenderContext): Panel() {

    private val selectedEntity = Label("Selected: N/A")
    private lateinit var modelPanel: ScrollablePanel

    private val modelIcon = ResourceMap["model"]
    private val deleteIcon = ResourceMap["delete"]

    init {
        style.display = Style.DisplayType.FLEX
        style.flexStyle.flexDirection = FlexStyle.FlexDirection.COLUMN
        style.setMargin(5f, 5f, 0f, 5f)
        style.position = Style.PositionType.RELATIVE

        val maxWidth = 175f
        style.setMaxWidth(maxWidth)
        style.flexStyle.flexGrow = 1

        style.background.color = ColorConstants.darkGray()
        isFocusable = false

        val title = Label("Entity Manager")
        title.setSizeLimits(maxWidth, 15f)
        title.style.background.color = Colour.GRAY.rgba
        title.textState.horizontalAlign = HorizontalAlign.CENTER
        add(title)

        selectedEntity.setSizeLimits(maxWidth, 15f)
        selectedEntity.style.setMarginTop(20f)
        selectedEntity.textState.horizontalAlign = HorizontalAlign.CENTER
        add(selectedEntity)

        addPolygonModes()
        addShadingTypes()
        addModelPanel()
    }

    private fun addPolygonModes() {
        val modes = ConfigGroup(
            Vector2f(6f, 41f), Vector2f(22f, 22f),
            arrayOf(ResourceMap["fill-cube"], ResourceMap["line-cube"], ResourceMap["point-cube"]),
            arrayOf("Fill", "Wireframe", "Vertices")
        )
        for ((i, button) in modes.buttons.withIndex()) {
            button.listenerMap.addListener(MouseClickEvent::class.java) { event ->
                if (event.button == Mouse.MouseButton.MOUSE_BUTTON_LEFT &&
                    event.action == MouseClickEvent.MouseClickAction.CLICK) {
                    when (i) {
                        0 -> context.framebuffer.polygonMode = PolygonMode.FILL
                        1 -> context.framebuffer.polygonMode = PolygonMode.LINE
                        2 -> context.framebuffer.polygonMode = PolygonMode.POINT
                    }
                }
            }
        }
        add(modes)
    }

    private fun addShadingTypes() {
        val types = ConfigGroup(
            Vector2f(88f, 41f), Vector2f(22f, 22f),
            arrayOf(ResourceMap["smooth-shading"], ResourceMap["flat-shading"], ResourceMap["no-shading"]),
            arrayOf("Smooth", "Flat", "None")
        )
        for ((i, button) in types.buttons.withIndex()) {
            button.listenerMap.addListener(MouseClickEvent::class.java) { event ->
                if (event.button == Mouse.MouseButton.MOUSE_BUTTON_LEFT &&
                    event.action == MouseClickEvent.MouseClickAction.CLICK) {
                    when (i) {
                        0 -> {
                            context.framebuffer.shadingType = ShadingType.SMOOTH
                            context.entity!!.reload(context.entityHandler)
                        } 1 -> {
                            context.framebuffer.shadingType = ShadingType.FLAT
                            context.entity!!.reload(context.entityHandler)
                        } 2 -> context.framebuffer.shadingType = ShadingType.NONE
                    }
                }
            }
        }
        add(types)
    }

    private fun addModelPanel() {
        modelPanel = ScrollablePanel()

        val width = 159f
        modelPanel.style.setMaxWidth(width)
        modelPanel.container.size.x = width - 8f

        modelPanel.style.setMargin(79f, 0f, 10f, 6f)
        modelPanel.style.position = Style.PositionType.RELATIVE
        modelPanel.style.flexStyle.flexGrow = 1
        modelPanel.viewport.style.setBottom(1f)
        modelPanel.remove(modelPanel.horizontalScrollBar)

        modelPanel.verticalScrollBar.style.setBottom(0f)
        modelPanel.verticalScrollBar.style.setWidth(8f)
        modelPanel.verticalScrollBar.style.setBorderRadius(0f)
        modelPanel.verticalScrollBar.style.border.isEnabled = false
        modelPanel.verticalScrollBar.style.background.color = ColorConstants.darkGray()

        val components = arrayOf(modelPanel.viewport, modelPanel.container, modelPanel)
        components.forEach {
            it.style.border.isEnabled = false
            it.style.background.color = ColorUtil.fromInt(71, 71, 71, 1f)
        }
        add(modelPanel)
    }

    fun update(entity: Entity) {
        val name = if (entity.name.length < 20) entity.name else entity.name.split(" ").first() // Trim if necessary
        selectedEntity.textState.text = "Selected: $name"

        val init = 3
        val offset = 17f
        modelPanel.container.size.y = init + entity.composition.size * offset
        modelPanel.container.clearChildComponents()

        for ((i, component) in entity.composition.withIndex()) {
            val y = init + i * offset
            val background = Label("", 3f, y, 145f, 15f)
            background.style.background.color = Colour.GRAY.rgba

            val label = Label("Model ${component.id}", 21f, y, 50f, 15f)
            val modelImage = ImageView(modelIcon)
            modelImage.position = Vector2f(7f, y + 3)
            modelImage.style.border.isEnabled = false

            val deleteButton = ImageView(deleteIcon)
            deleteButton.position = Vector2f(136f, y + 3)
            deleteButton.style.border.isEnabled = false
            deleteButton.listenerMap.addListener(MouseClickEvent::class.java) { event ->
                if (event.action == MouseClickEvent.MouseClickAction.CLICK) {
                    entity.remove(component, context.entityHandler)
                    update(entity)
                }
            }

            val components = arrayOf(background, label, modelImage, deleteButton)
            components.forEach { modelPanel.container.add(it) }
        }
    }
}