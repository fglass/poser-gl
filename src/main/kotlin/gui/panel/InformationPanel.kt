package gui.panel

import BG_COLOUR
import Processor
import RESOURCES_PATH
import entity.Entity
import gui.Gui
import org.joml.Vector2f
import org.joml.Vector4f
import org.liquidengine.legui.component.ImageView
import org.liquidengine.legui.component.Label
import org.liquidengine.legui.component.Panel
import org.liquidengine.legui.component.optional.align.HorizontalAlign
import org.liquidengine.legui.event.MouseClickEvent
import org.liquidengine.legui.image.BufferedImage
import org.liquidengine.legui.style.color.ColorConstants

class InformationPanel(private val gui: Gui, private val context: Processor): Panel() {

    private var npcName: Label
    private var npcId: Label
    private val modelPanel: Panel
    private val modelIcon = BufferedImage(RESOURCES_PATH + "model.png")
    private val deleteIcon = BufferedImage(RESOURCES_PATH + "delete.png")

    init {
        position = getPanelPosition()
        size = getPanelSize()
        style.background.color = ColorConstants.darkGray()
        isFocusable = false

        val title = Label("Information", 0f, 5f, size.x, 15f)
        title.textState.horizontalAlign = HorizontalAlign.CENTER
        val name = Label("Name:", 5f, 25f, 80f, 15f)
        npcName = Label("N/A", 60f, 25f, 104f, 15f)
        npcName.textState.horizontalAlign = HorizontalAlign.RIGHT

        val id = Label("Id:", 5f, 40f, 100f, 15f)
        npcId = Label("N/A", 60f, 40f, 104f, 15f)
        npcId.textState.horizontalAlign = HorizontalAlign.RIGHT

        val models = Label("Composition:", 5f, 55f, 100f, 15f)
        modelPanel = Panel(5f, 72f, 159f, 16f)
        modelPanel.style.border.isEnabled = false
        modelPanel.style.background.color = ColorConstants.darkGray()

        add(title)
        add(name)
        add(npcName)
        add(id)
        add(npcId)
        add(modelPanel)
        add(models)
    }

    fun update(entity: Entity) {
        npcName.textState.text = entity.npc.name
        val id = entity.npc.id
        npcId.textState.text = if (id >= 0) id.toString() else "N/A"

        val offset = 16f
        modelPanel.removeAll(modelPanel.childComponents)
        modelPanel.size.y = entity.composition.size * offset

        for ((i, model) in entity.composition.withIndex()) {
            val y = i * offset
            val empty = Label("", 0f, y, 159f, 15f )
            empty.style.background.color = Vector4f(BG_COLOUR, BG_COLOUR, BG_COLOUR, 1f)

            val label = Label("Model $model", 18f, y, 139f, 15f)
            val modelImage = ImageView(modelIcon)
            modelImage.position = Vector2f(4f, y + 3)
            modelImage.style.border.isEnabled = false

            val deleteButton = ImageView(deleteIcon)
            deleteButton.position = Vector2f(145f, y + 3)
            deleteButton.style.border.isEnabled = false
            deleteButton.listenerMap.addListener(MouseClickEvent::class.java) { event ->
                if (event.action == MouseClickEvent.MouseClickAction.CLICK) {
                    entity.remove(model, context.npcLoader)
                    update(entity)
                }
            }

            modelPanel.add(empty)
            modelPanel.add(label)
            modelPanel.add(modelImage)
            modelPanel.add(deleteButton)
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
        return Vector2f(170f, gui.size.y - 144)
    }
}