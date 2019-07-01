package gui.panel

import BG_COLOUR
import Processor
import RESOURCES_PATH
import entity.Entity
import gui.GuiManager
import org.joml.Vector2f
import org.liquidengine.legui.component.ImageView
import org.liquidengine.legui.component.Label
import org.liquidengine.legui.component.Panel
import org.liquidengine.legui.component.ScrollablePanel
import org.liquidengine.legui.component.optional.align.HorizontalAlign
import org.liquidengine.legui.event.MouseClickEvent
import org.liquidengine.legui.image.BufferedImage
import org.liquidengine.legui.style.color.ColorConstants

class TreePanel(private val gui: GuiManager, private val context: Processor): Panel() {

    private val modelPanel: ScrollablePanel
    private val modelIcon = BufferedImage(RESOURCES_PATH + "model.png")
    private val deleteIcon = BufferedImage(RESOURCES_PATH + "delete.png")

    init {
        position = getPanelPosition()
        size = getPanelSize()
        style.background.color = ColorConstants.darkGray()
        isFocusable = false

        val title = Label("Composition Tree", 0f, 0f, size.x, 16f)
        title.style.background.color = BG_COLOUR
        title.textState.horizontalAlign = HorizontalAlign.CENTER
        add(title)

        modelPanel = ScrollablePanel(5f, 25f, 160f, 127f)
        modelPanel.remove(modelPanel.horizontalScrollBar)

        val components = arrayOf(modelPanel, modelPanel.viewport, modelPanel.container)
        components.forEach {
            it.style.border.isEnabled = false
            it.style.background.color = ColorConstants.darkGray()
        }
        add(modelPanel)
    }

    fun update(entity: Entity) {
        val offset = 17f
        modelPanel.container.removeAll(modelPanel.container.childComponents)
        modelPanel.container.size.y = entity.composition.size * offset

        for ((i, model) in entity.composition.withIndex()) {
            val y = i * offset
            val background = Label("", 0f, y, 148f, 15f)
            background.style.background.color = BG_COLOUR

            val label = Label("Model $model", 18f, y, 139f, 15f)
            val modelImage = ImageView(modelIcon)
            modelImage.position = Vector2f(4f, y + 3)
            modelImage.style.border.isEnabled = false

            val deleteButton = ImageView(deleteIcon)
            deleteButton.position = Vector2f(136f, y + 3)
            deleteButton.style.border.isEnabled = false
            deleteButton.listenerMap.addListener(MouseClickEvent::class.java) { event ->
                if (event.action == MouseClickEvent.MouseClickAction.CLICK) {
                    entity.remove(model, context.entityHandler)
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
        return Vector2f(gui.size.x - 175, 27f)
    }

    private fun getPanelSize(): Vector2f {
        return Vector2f(170f, gui.size.y - 409)
    }
}