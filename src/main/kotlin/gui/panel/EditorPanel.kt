package gui.panel

import BG_COLOUR
import RESOURCES_PATH
import Processor
import animation.Animation
import animation.Keyframe
import animation.Reference
import animation.TransformationType
import animation.node.ReferenceNode
import gui.GuiManager
import gui.component.ButtonGroup
import gui.component.ConfigGroup
import gui.component.TextSlider
import org.joml.Vector2f
import org.joml.Vector4f
import org.liquidengine.legui.component.Label
import org.liquidengine.legui.component.Panel
import org.liquidengine.legui.component.optional.align.HorizontalAlign
import org.liquidengine.legui.event.MouseClickEvent
import org.liquidengine.legui.image.BufferedImage
import org.liquidengine.legui.input.Mouse
import org.liquidengine.legui.style.color.ColorConstants

class EditorPanel(private val gui: GuiManager, private val context: Processor): Panel() {

    private val sliders = ArrayList<TextSlider>()
    private var currentReference: Reference? = null
    private val selectedFrame: Label
    private val frameLength: TextSlider
    private val selectedNode: Label

    private val addIcon = BufferedImage(RESOURCES_PATH + "add-keyframe.png")
    private val copyIcon = BufferedImage(RESOURCES_PATH + "copy.png")
    private val pasteIcon = BufferedImage(RESOURCES_PATH + "paste.png")
    private val deleteIcon = BufferedImage(RESOURCES_PATH + "delete-keyframe.png")

    private val referenceIcon = BufferedImage(RESOURCES_PATH + "reference.png")
    private val translationIcon = BufferedImage(RESOURCES_PATH + "translation.png")
    private val rotationIcon = BufferedImage(RESOURCES_PATH + "rotation.png")
    private val scaleIcon = BufferedImage(RESOURCES_PATH + "scale.png")
    private val transformations: ConfigGroup

    init {
        position = getPanelPosition()
        size = getPanelSize()
        style.background.color = ColorConstants.darkGray()
        isFocusable = false

        val framePanel = Panel(0f, 0f, size.x, 90f)
        framePanel.style.background.color = ColorConstants.darkGray()
        framePanel.style.border.isEnabled = false
        add(framePanel)

        val frameTitle = Label("Keyframe Editor", 0f, 0f, size.x, 16f)
        frameTitle.style.background.color = BG_COLOUR
        frameTitle.textState.horizontalAlign = HorizontalAlign.CENTER
        framePanel.add(frameTitle)

        selectedFrame = Label("Selected: N/A", 0f, 20f, size.x, 15f)
        selectedFrame.textState.horizontalAlign = HorizontalAlign.CENTER
        framePanel.add(selectedFrame)

        val length = Label("Length:", 35f, 40f, 50f, 15f)
        framePanel.add(length)

        frameLength = TextSlider({ context.animationHandler.currentFrame.changeLength(it, context) },
                                 Pair(1, 99), 82f, 40f, 50f, 15f)
        framePanel.add(frameLength)

        val icons = ButtonGroup(Vector2f(31f, 59f), Vector2f(24f, 24f), addIcon, copyIcon, pasteIcon, deleteIcon)
        icons.style.background.color = ColorConstants.transparent()
        val operations = arrayOf(Animation::addKeyframe, Animation::copyKeyframe,
                                 Animation::pasteKeyframe, Animation::deleteKeyframe)

        for ((i, button) in icons.buttons.withIndex()) {
            button.listenerMap.addListener(MouseClickEvent::class.java) { event ->
                if (event.button == Mouse.MouseButton.MOUSE_BUTTON_LEFT &&
                    event.action == MouseClickEvent.MouseClickAction.CLICK) {
                    val animation = context.animationHandler.currentAnimation?: return@addListener
                    operations[i].invoke(animation)
                }
            }
        }
        framePanel.add(icons)

        val nodePanel = Panel(0f, 95f, size.x, 150f)
        nodePanel.style.background.color = ColorConstants.darkGray()
        nodePanel.style.border.isEnabled = false
        add(nodePanel)

        val nodeTitle = Label("Node Transformer", 0f, 0f, size.x, 16f)
        nodeTitle.style.background.color = BG_COLOUR
        nodeTitle.textState.horizontalAlign = HorizontalAlign.CENTER
        nodePanel.add(nodeTitle)

        selectedNode = Label("Selected: N/A", 0f, 20f, size.x, 15f)
        selectedNode.textState.horizontalAlign = HorizontalAlign.CENTER
        nodePanel.add(selectedNode)

        transformations = ConfigGroup(Vector2f(31f, 41f), Vector2f(24f, 24f),
                                      referenceIcon, translationIcon, rotationIcon, scaleIcon)

        for ((i, button) in transformations.buttons.withIndex()) {
            button.listenerMap.addListener(MouseClickEvent::class.java) { event ->
                if (event.button == Mouse.MouseButton.MOUSE_BUTTON_LEFT &&
                    event.action == MouseClickEvent.MouseClickAction.CLICK) {
                    updateType(TransformationType.fromId(i))
                }
            }
        }
        nodePanel.add(transformations)

        val transformPanel = Panel(31f, 80f, 108f, 78f)
        val colour = 71 / 255f
        transformPanel.style.background.color = Vector4f(colour, colour, colour, 1f)
        transformPanel.style.border.isEnabled = false
        nodePanel.add(transformPanel)

        var y = 7f
        val coords = arrayOf("X", "Y", "Z")

        for ((i, coord) in coords.withIndex()){
            val label = Label(coord, 16f, y, 50f, 15f)
            transformPanel.add(label)

            val slider = TextSlider({ context.animationHandler.transformNode(i, it) },
                                    Pair(-255, 255), 39f, y, 60f, 15f)
            sliders.add(slider)
            transformPanel.add(slider)
            y += 20
        }
    }

    fun setKeyframe(keyframe: Keyframe) {
        selectedFrame.textState.text = "Selected: ${keyframe.id}"
        frameLength.setValue(keyframe.length)
    }

    fun setNode(node: ReferenceNode, selectedType: TransformationType) {
        for ((i, button) in transformations.buttons.withIndex()) {
            val type = TransformationType.fromId(i)
            button.isFocusable = node.reference.group[type] != null
        }
        transformations.updateConfigs(transformations.buttons[selectedType.id])

        currentReference = node.reference
        selectedNode.textState.text = "Selected: ${node.reference.id}"
        updateType(selectedType)
    }

    private fun updateType(type: TransformationType) {
        context.framebuffer.nodeRenderer.selectedType = type
        val reference = currentReference?: return
        val transformation = reference.group[type]?: return

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
        return Vector2f(gui.size.x - 175, gui.size.y - 377)
    }

    private fun getPanelSize(): Vector2f {
        return Vector2f(170f, 254f)
    }
}