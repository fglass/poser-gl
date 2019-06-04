package gui

import net.runelite.cache.definitions.NpcDefinition
import org.joml.Vector2f
import org.liquidengine.legui.component.Label
import org.liquidengine.legui.component.Widget
import org.liquidengine.legui.component.optional.align.HorizontalAlign

class InformationWidget(width: Float, height: Float, private val gui: Gui) : Widget() {

    private var npcName: Label
    private var npcId: Label
    private var npcModels: Label
    var animationId: Label

    init {
        title.textState.text = "Information"
        position = getWidgetPosition()
        size = Vector2f(width, height)
        isResizable = false
        isDraggable = false
        isCloseable = false

        val name = Label("Name:", 5f, 5f, 80f, 15f)
        npcName = Label("N/A", 60f, 5f, 104f, 15f)
        npcName.textState.horizontalAlign = HorizontalAlign.RIGHT

        val id = Label("Id:", 5f, 20f, 100f, 15f)
        npcId = Label("N/A", 60f, 20f, 104f, 15f)
        npcId.textState.horizontalAlign = HorizontalAlign.RIGHT

        val animation = Label("Animation:", 5f, 35f, 100f, 15f)
        animationId = Label("N/A", 60f, 35f, 104f, 15f)
        animationId.textState.horizontalAlign = HorizontalAlign.RIGHT

        val models = Label("Model composition:", 5f, 50f, 100f, 15f)
        npcModels = Label("N/A", 5f, 65f, 159f, 15f)
        npcModels.textState.horizontalAlign = HorizontalAlign.RIGHT

        container.add(name)
        container.add(npcName)
        container.add(id)
        container.add(npcId)
        container.add(animation)
        container.add(animationId)
        container.add(models)
        container.add(npcModels)
    }

    fun update(npc: NpcDefinition) {
        npcName.textState.text = npc.name
        npcId.textState.text = npc.id.toString()
        animationId.textState.text = "N/A"
        npcModels.textState.text = npc.models?.contentToString()
    }

    fun getWidgetPosition(): Vector2f {
        return Vector2f(gui.size.x - 180, 5f)
    }
}