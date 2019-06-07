package gui

import BG_COLOUR
import net.runelite.cache.definitions.NpcDefinition
import org.joml.Vector2f
import org.joml.Vector4f
import org.liquidengine.legui.component.*
import org.liquidengine.legui.component.optional.align.HorizontalAlign
import org.liquidengine.legui.style.color.ColorConstants

class InformationPanel(private val gui: Gui) : Panel() {

    private var npcName: Label
    private var npcId: Label
    var animationId: Label
    private val modelPanel: Panel

    init {
        position = getPanelPosition()
        size = getPanelSize()
        style.background.color = ColorConstants.darkGray()

        val title = Label("Information", 0f, 0f, size.x, 15f)
        title.textState.horizontalAlign = HorizontalAlign.CENTER
        val name = Label("Name:", 5f, 20f, 80f, 15f)
        npcName = Label("N/A", 60f, 20f, 104f, 15f)
        npcName.textState.horizontalAlign = HorizontalAlign.RIGHT

        val id = Label("Id:", 5f, 35f, 100f, 15f)
        npcId = Label("N/A", 60f, 35f, 104f, 15f)
        npcId.textState.horizontalAlign = HorizontalAlign.RIGHT

        val animation = Label("Animation:", 5f, 50f, 100f, 15f)
        animationId = Label("N/A", 60f, 50f, 104f, 15f)
        animationId.textState.horizontalAlign = HorizontalAlign.RIGHT

        val models = Label("Composition:", 5f, 65f, 100f, 15f)
        modelPanel = Panel(5f, 81f, 159f, 15f)
        modelPanel.style.border.isEnabled = false
        modelPanel.style.background.color = ColorConstants.darkGray()

        add(title)
        add(name)
        add(npcName)
        add(id)
        add(npcId)
        add(animation)
        add(animationId)
        add(modelPanel)
        add(models)
    }

    fun update(npc: NpcDefinition) {
        npcName.textState.text = npc.name
        npcId.textState.text = npc.id.toString()
        animationId.textState.text = "N/A"

        val offset = 16f
        modelPanel.removeAll(modelPanel.childComponents)
        modelPanel.size.y = npc.models.size * offset

        for ((i, model) in npc.models.withIndex()) {
            val label = Label("Model $model", 0f, i * offset, 159f, 15f)
            label.textState.horizontalAlign = HorizontalAlign.RIGHT
            label.style.background.color = Vector4f(BG_COLOUR, BG_COLOUR, BG_COLOUR, 1f)
            modelPanel.add(label)
        }
    }

    fun resize() {
        position = getPanelPosition()
        size = getPanelSize()
    }

    private fun getPanelPosition(): Vector2f {
        return Vector2f(gui.size.x - 175, 49f)
    }

    private fun getPanelSize(): Vector2f {
        return Vector2f(170f, gui.size.y - 159)
    }
}