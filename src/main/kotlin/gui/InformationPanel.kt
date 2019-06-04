package gui

import net.runelite.cache.definitions.NpcDefinition
import org.joml.Vector2f
import org.liquidengine.legui.component.*
import org.liquidengine.legui.component.optional.align.HorizontalAlign

class InformationPanel(width: Float, private val height: Float, private val gui: Gui) : Panel() {

    private var npcName: Label
    private var npcId: Label
    var animationId: Label
    private val modelPanel: Panel

    init {
        position = getWidgetPosition()
        size = Vector2f(width, height)

        val title = Label("Information", 0f, 0f, width, 15f)
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

        val models = Label("Model composition:", 5f, 65f, 100f, 15f)
        modelPanel = Panel(5f, 65f, 159f, 15f)
        modelPanel.style.border.isEnabled = false

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

        val offset = 15f
        modelPanel.removeAll(modelPanel.childComponents)
        modelPanel.size.y = npc.models.size * offset
        size.y = height + modelPanel.size.y - offset

        for ((i, model) in npc.models.withIndex()) {
            val label = Label(model.toString(), 0f, i * offset, 159f, 15f)
            label.textState.horizontalAlign = HorizontalAlign.RIGHT
            modelPanel.add(label)
        }
    }

    fun resize() {
        position = getWidgetPosition()
    }

    private fun getWidgetPosition(): Vector2f {
        return Vector2f(gui.size.x - 180, 5f)
    }
}