package gui.panel

import SPRITE_PATH
import BG_COLOUR
import Processor
import entity.Entity
import gui.component.ConfigGroup
import org.joml.Vector2f
import org.joml.Vector4f
import org.liquidengine.legui.component.ImageView
import org.liquidengine.legui.component.Label
import org.liquidengine.legui.component.Panel
import org.liquidengine.legui.component.ScrollablePanel
import org.liquidengine.legui.component.optional.align.HorizontalAlign
import org.liquidengine.legui.event.MouseClickEvent
import org.liquidengine.legui.event.MouseDragEvent
import org.liquidengine.legui.event.ScrollEvent
import org.liquidengine.legui.image.BufferedImage
import org.liquidengine.legui.input.Mouse
import org.liquidengine.legui.style.Style
import org.liquidengine.legui.style.color.ColorConstants
import org.liquidengine.legui.style.color.ColorUtil
import org.liquidengine.legui.style.flex.FlexStyle
import org.liquidengine.legui.style.length.LengthType.PIXEL
import render.PolygonMode
import shader.ShadingType
import util.setSizeLimits
import kotlin.math.max

class ManagerPanel(private val context: Processor): Panel() {

    private val selectedEntity = Label("Selected: N/A")
    private lateinit var modelPanel: ScrollablePanel

    private val modelIcon = BufferedImage(SPRITE_PATH + "model.png")
    private val deleteIcon = BufferedImage(SPRITE_PATH + "delete.png")

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
        title.style.background.color = BG_COLOUR
        title.textState.horizontalAlign = HorizontalAlign.CENTER
        add(title)

        selectedEntity.setSizeLimits(maxWidth, 15f)
        selectedEntity.style.marginTop = PIXEL.length(20f)
        selectedEntity.textState.horizontalAlign = HorizontalAlign.CENTER
        add(selectedEntity)

        addPolygonModes()
        addShadingTypes()
        addModelPanel()
    }

    private fun addPolygonModes() {
        val modes = ConfigGroup(
            Vector2f(6f, 41f), Vector2f(22f, 22f),
            arrayOf(
                BufferedImage(SPRITE_PATH + "fill-cube.png"), BufferedImage(SPRITE_PATH + "line-cube.png"),
                BufferedImage(SPRITE_PATH + "point-cube.png")), arrayOf("Fill", "Wireframe", "Vertices")
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
            arrayOf(BufferedImage(SPRITE_PATH + "smooth-shading.png"), BufferedImage(SPRITE_PATH + "flat-shading.png"),
                BufferedImage(SPRITE_PATH + "no-shading.png")), arrayOf("Smooth", "Flat", "None")
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

        val width = 160f
        modelPanel.style.setMaxWidth(width)
        modelPanel.container.size.x = width - 8f

        modelPanel.style.setMargin(79f, 0f, 6f, 6f)
        modelPanel.style.position = Style.PositionType.RELATIVE
        modelPanel.style.flexStyle.flexGrow = 1

        modelPanel.remove(modelPanel.horizontalScrollBar)
        modelPanel.viewport.style.setBottom(0f)
        modelPanel.verticalScrollBar.style.setBottom(0f)

        val containers = arrayOf(modelPanel.viewport, modelPanel.container)
        containers.forEach {
            it.style.border.isEnabled = false
            it.style.background.color = ColorUtil.fromInt(71, 71, 71, 1f)
        }

        val components = arrayOf(modelPanel, modelPanel.verticalScrollBar)
        components.forEach {
            it.style.border.isEnabled = false
            it.style.background.color = ColorConstants.darkGray()
        }

        modelPanel.verticalScrollBar.style.setWidth(9f)
        add(modelPanel)
    }

    fun update(entity: Entity) {
        modelPanel.container.removeAll(modelPanel.container.childComponents)

        val name = if (entity.name.length < 20) entity.name else entity.name.split(" ").first() // Trim if necessary
        selectedEntity.textState.text = "Selected: $name"

        val init = 3
        val offset = 17f
        modelPanel.container.size.y = max(init + entity.composition.size * offset + 1, modelPanel.size.y + 1)

        for ((i, component) in entity.composition.withIndex()) {
            val y = init + i * offset
            val background = Label("", 3f, y, 145f, 15f)
            background.style.background.color = BG_COLOUR

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