package gui.panel

import Processor
import RESOURCES_PATH
import animation.Reference
import animation.TransformationType
import animation.node.ReferenceNode
import gui.Gui
import gui.component.TextSlider
import gui.component.ConfigGroup
import org.joml.Vector2f
import org.liquidengine.legui.component.Label
import org.liquidengine.legui.component.Panel
import org.liquidengine.legui.component.optional.align.HorizontalAlign
import org.liquidengine.legui.event.MouseClickEvent
import org.liquidengine.legui.image.BufferedImage
import org.liquidengine.legui.input.Mouse
import org.liquidengine.legui.style.color.ColorConstants

class EditorPanel(private val gui: Gui, private val context: Processor): Panel() {

    private val sliders = ArrayList<TextSlider>()
    private var currentReference: Reference? = null

    private val referenceIcon = BufferedImage(RESOURCES_PATH + "reference.png")
    private val translationIcon = BufferedImage(RESOURCES_PATH + "translation.png")
    private val rotationIcon = BufferedImage(RESOURCES_PATH + "rotation.png")
    private val scaleIcon = BufferedImage(RESOURCES_PATH + "scale.png")
    private val transformations = ConfigGroup(31f, 30f, referenceIcon, translationIcon, rotationIcon, scaleIcon)

    init {
        position = getPanelPosition()
        size = getPanelSize()
        style.background.color = ColorConstants.darkGray()
        isFocusable = false

        val title = Label("Keyframe Editor", 0f, 5f, size.x, 15f)
        title.textState.horizontalAlign = HorizontalAlign.CENTER
        add(title)

        for ((i, button) in transformations.buttons.withIndex()) {
            button.listenerMap.addListener(MouseClickEvent::class.java) { event ->
                if (event.button == Mouse.MouseButton.MOUSE_BUTTON_LEFT &&
                    event.action == MouseClickEvent.MouseClickAction.CLICK) {
                    updateType(TransformationType.fromInt(i))
                }
            }
        }
        add(transformations)

        var y = 70f
        val coords = arrayOf("X", "Y", "Z")

        for ((i, coord) in coords.withIndex()){
            val label = Label(coord, 41f, y, 50f, 15f)
            add(label)

            val slider = TextSlider(context, i, 63f, y, 75f, 15f) //139
            sliders.add(slider)
            add(slider)
            y += 20
        }
    }

    fun setNode(node: ReferenceNode, selectedType: TransformationType) {
        for ((i, button) in transformations.buttons.withIndex()) {
            val type = TransformationType.fromInt(i)
            button.isFocusable = node.reference.group[type] != null
        }
        transformations.updateConfigs(transformations.buttons[selectedType.id])

        currentReference = node.reference
        updateType(selectedType)
    }

    private fun updateType(type: TransformationType) {
        val reference = currentReference?: return
        val transformation = reference.group[type]?: return

        context.framebuffer.nodeRenderer.selectedType = type
        for (i in 0 until sliders.size) {
            sliders[i].setValue(transformation.offset.get(i))
        }
    }

    fun resetSliders() {
        sliders.forEach { it.setValue(0) }
    }

    fun resize() {
        position = getPanelPosition()
        size = getPanelSize()
    }

    private fun getPanelPosition(): Vector2f {
        return Vector2f(gui.size.x - 175, gui.size.y - 421)
    }

    private fun getPanelSize(): Vector2f {
        return Vector2f(170f, 298f)
    }
}