package gui.panel

import SPRITE_PATH
import BG_COLOUR
import Processor
import animation.*
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
import org.liquidengine.legui.style.Style
import org.liquidengine.legui.style.color.ColorConstants
import org.liquidengine.legui.style.flex.FlexStyle
import org.liquidengine.legui.style.length.LengthType.PIXEL
import util.setSizeLimits

class EditorPanel(private val context: Processor): Panel() { // TODO: Clean-up

    private val sliders = ArrayList<TextSlider>()
    private var currentReference: ReferenceNode? = null
    private lateinit var selectedFrame: Label
    private lateinit var frameLength: TextSlider
    private val selectedNode: Label
    private val transformations: ConfigGroup

    init {
        style.display = Style.DisplayType.FLEX
        style.flexStyle.flexDirection = FlexStyle.FlexDirection.COLUMN

        val sizeX = 170f
        setSizeLimits(sizeX, 260f)

        style.setMargin(5f, 5f, 5f, 5f)
        style.position = Style.PositionType.RELATIVE
        style.background.color = ColorConstants.darkGray()
        isFocusable = false

        addFramePanel(sizeX)
        
        val nodePanel = Panel()
        nodePanel.setSizeLimits(sizeX, 155f)
        nodePanel.style.marginTop = PIXEL.length(95f)
        nodePanel.style.background.color = ColorConstants.darkGray()
        nodePanel.style.border.isEnabled = false
        add(nodePanel)

        val nodeTitle = Label("Node Transformer", 0f, 0f, sizeX, 16f)
        nodeTitle.style.background.color = BG_COLOUR
        nodeTitle.textState.horizontalAlign = HorizontalAlign.CENTER
        nodePanel.add(nodeTitle)

        selectedNode = Label("Selected: N/A", 0f, 20f, sizeX, 15f)
        selectedNode.textState.horizontalAlign = HorizontalAlign.CENTER
        nodePanel.add(selectedNode)

        transformations = ConfigGroup(Vector2f(31f, 41f), Vector2f(24f, 24f),
            arrayOf(BufferedImage(SPRITE_PATH + "reference.png"), BufferedImage(SPRITE_PATH + "translation.png"),
            BufferedImage(SPRITE_PATH + "rotation.png"), BufferedImage(SPRITE_PATH + "scale.png")),
            arrayOf("Reference", "Translation", "Rotation", "Scale"))

        transformations.position = Vector2f(31f, 41f)
        for ((i, button) in transformations.buttons.withIndex()) {
            button.listenerMap.addListener(MouseClickEvent::class.java) { event ->
                if (event.button == Mouse.MouseButton.MOUSE_BUTTON_LEFT &&
                    event.action == MouseClickEvent.MouseClickAction.CLICK) {
                    updateType(TransformationType.fromId(i))
                }
            }
        }
        nodePanel.add(transformations)

        val transformPanel = Panel(32f, 80f, 106f, 70f)
        val colour = 71 / 255f
        transformPanel.style.background.color = Vector4f(colour, colour, colour, 1f)
        transformPanel.style.border.isEnabled = false
        nodePanel.add(transformPanel)

        var y = 7f
        val coords = arrayOf("X", "Y", "Z")

        for ((i, coord) in coords.withIndex()) {
            val label = Label(coord, 12f, y, 50f, 15f)
            transformPanel.add(label)

            val slider = TextSlider({ context.animationHandler.transformNode(i, it) },
                                    Pair(-255, 255), 35f, y, 60f, 15f)
            sliders.add(slider)
            transformPanel.add(slider)
            y += 20
        }
    }

    private fun addFramePanel(sizeX: Float) {
        val framePanel = Panel()
        framePanel.setSizeLimits(sizeX, 90f)
        framePanel.style.background.color = ColorConstants.darkGray()
        framePanel.style.border.isEnabled = false
        add(framePanel)

        val frameTitle = Label("Keyframe Editor", 0f, 0f, sizeX, 16f)
        frameTitle.style.background.color = BG_COLOUR
        frameTitle.textState.horizontalAlign = HorizontalAlign.CENTER
        framePanel.add(frameTitle)

        selectedFrame = Label("Selected: N/A", 0f, 20f, sizeX, 15f)
        selectedFrame.textState.horizontalAlign = HorizontalAlign.CENTER
        framePanel.add(selectedFrame)

        val length = Label("Length:", 35f, 40f, 50f, 15f)
        framePanel.add(length)

        frameLength = TextSlider({ context.animationHandler.getAnimation(false)?.changeKeyframeLength(it) },
            Pair(1, 99), 81f, 40f, 51f, 15f)
        framePanel.add(frameLength)

        val actions = ButtonGroup(
            Vector2f(23f, 59f), Vector2f(23f, 23f), arrayOf(KeyframeAction.ADD.getIcon(false),
                KeyframeAction.COPY.getIcon(false), KeyframeAction.PASTE.getIcon(false),
                KeyframeAction.INTERPOLATE.getIcon(false), KeyframeAction.DELETE.getIcon(false)),
            arrayOf("Add", "Copy" ,"Paste", "Interpolate", "Delete")
        )

        for ((i, button) in actions.buttons.withIndex()) {
            val action = KeyframeAction.values()[i]
            button.listenerMap.addListener(MouseClickEvent::class.java) { event ->
                if (event.button == Mouse.MouseButton.MOUSE_BUTTON_LEFT &&
                    event.action == MouseClickEvent.MouseClickAction.CLICK) {
                    action.apply(context)
                }
            }
            button.addHover(action.getIcon(true))
        }
        actions.style.background.color = ColorConstants.transparent()
        actions.position = Vector2f(23f, 59f)
        framePanel.add(actions)
    }

    fun setKeyframe(keyframe: Keyframe) {
        selectedFrame.textState.text = "Selected: ${keyframe.id}"
        frameLength.setValue(keyframe.length)
    }

    fun setNode(node: ReferenceNode, selectedType: TransformationType) {
        for ((i, button) in transformations.buttons.withIndex()) {
            val type = TransformationType.fromId(i)
            button.isFocusable = node.getTransformation(type) != null
        }
        transformations.updateConfigs(transformations.buttons[selectedType.id])

        currentReference = node
        selectedNode.textState.text = "Selected: ${node.id}"
        updateType(selectedType)
    }

    private fun updateType(type: TransformationType) {
        context.nodeRenderer.selectedType = type
        val transformation = currentReference?.getTransformation(type)?: return

        for (i in 0 until sliders.size) {
            sliders[i].setValue(transformation.delta.get(i))
        }
    }

    fun reset() {
        val unselected = "Selected: N/A"
        selectedFrame.textState.text = unselected
        selectedNode.textState.text = unselected
        frameLength.setValue(0)
        sliders.forEach { it.setValue(0) }
    }
}