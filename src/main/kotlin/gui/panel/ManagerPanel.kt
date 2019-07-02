package gui.panel

import SPRITE_PATH
import BG_COLOUR
import Processor
import entity.Entity
import gui.GuiManager
import gui.component.ConfigGroup
import org.joml.Vector2f
import org.joml.Vector4f
import org.liquidengine.legui.component.ImageView
import org.liquidengine.legui.component.Label
import org.liquidengine.legui.component.Panel
import org.liquidengine.legui.component.ScrollablePanel
import org.liquidengine.legui.component.optional.align.HorizontalAlign
import org.liquidengine.legui.event.MouseClickEvent
import org.liquidengine.legui.image.BufferedImage
import org.liquidengine.legui.input.Mouse
import org.liquidengine.legui.style.color.ColorConstants
import render.PolygonMode
import shader.ShadingType
import kotlin.math.max

class ManagerPanel(private val gui: GuiManager, private val context: Processor): Panel() {

    private val selectedEntity = Label("Selected: N/A", 0f, 20f, 0f, 0f)
    private val modelPanel: ScrollablePanel
    private val modelIcon = BufferedImage(SPRITE_PATH + "model.png")
    private val deleteIcon = BufferedImage(SPRITE_PATH + "delete.png")

    init {
        position = getPanelPosition()
        size = getPanelSize()
        style.background.color = ColorConstants.darkGray()
        isFocusable = false

        val title = Label("Entity Manager", 0f, 0f, size.x, 16f)
        title.style.background.color = BG_COLOUR
        title.textState.horizontalAlign = HorizontalAlign.CENTER
        add(title)

        selectedEntity.size = Vector2f(size.x, 15f)
        selectedEntity.textState.horizontalAlign = HorizontalAlign.CENTER
        add(selectedEntity)

        val modes = ConfigGroup(
            Vector2f(6f, 41f), Vector2f(22f, 22f),
            BufferedImage(SPRITE_PATH + "fill-cube.png"), BufferedImage(SPRITE_PATH + "line-cube.png"),
            BufferedImage(SPRITE_PATH + "point-cube.png")
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

        val types = ConfigGroup(
            Vector2f(88f, 41f), Vector2f(22f, 22f),
            BufferedImage(SPRITE_PATH + "smooth-shading.png"), BufferedImage(SPRITE_PATH + "flat-shading.png"),
            BufferedImage(SPRITE_PATH + "no-shading.png")
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

        modelPanel = ScrollablePanel(6f, 78f, 160f, 130f)
        modelPanel.remove(modelPanel.horizontalScrollBar)

        val colour = 71 / 255f
        var components = arrayOf(modelPanel.viewport, modelPanel.container)
        components.forEach {
            it.style.border.isEnabled = false
            it.style.background.color = Vector4f(colour, colour, colour, 1f)
        }
        components = arrayOf(modelPanel, modelPanel.verticalScrollBar)
        components.forEach {
            it.style.border.isEnabled = false
            it.style.background.color = ColorConstants.darkGray()
        }

        modelPanel.verticalScrollBar.style.width = 9f
        add(modelPanel)
    }

    fun update(entity: Entity) {
        modelPanel.container.removeAll(modelPanel.container.childComponents)
        selectedEntity.textState.text = "Selected: ${entity.name}"

        val offset = 17f
        modelPanel.container.size.y = max(3 + entity.composition.size * offset, modelPanel.size.y - 7)

        for ((i, component) in entity.composition.withIndex()) {
            val y = 3 + i * offset
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

    fun resize() {
        position = getPanelPosition()
        size = getPanelSize()
    }

    private fun getPanelPosition(): Vector2f {
        return Vector2f(gui.size.x - 175, 5f)
    }

    private fun getPanelSize(): Vector2f {
        return Vector2f(170f, gui.size.y - 387)
    }
}