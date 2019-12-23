package gui.panel

import animation.Keyframe
import animation.KeyframeAction
import animation.ReferenceNode
import api.TransformationType
import animation.command.impl.ChangeLengthCommand
import animation.command.impl.TransformNodeCommand
import gui.component.ButtonGroup
import gui.component.ConfigGroup
import gui.component.TextSlider
import org.joml.Vector2f
import org.liquidengine.legui.component.Label
import org.liquidengine.legui.component.Panel
import org.liquidengine.legui.component.optional.align.HorizontalAlign
import org.liquidengine.legui.event.MouseClickEvent
import org.liquidengine.legui.input.Mouse
import org.liquidengine.legui.style.Style
import org.liquidengine.legui.style.color.ColorConstants
import org.liquidengine.legui.style.color.ColorUtil
import org.liquidengine.legui.style.flex.FlexStyle
import render.RenderContext
import util.Colour
import util.ResourceMap
import util.setSizeLimits

// TODO: reference node position editing - advanced mode

class EditorPanel(private val context: RenderContext): Panel() {

    val sliders = ArrayList<TextSlider>()
    private lateinit var selectedFrame: Label
    lateinit var frameLength: TextSlider
    private lateinit var selectedNode: Label
    private lateinit var transformations: ConfigGroup

    init {
        style.display = Style.DisplayType.FLEX
        style.flexStyle.flexDirection = FlexStyle.FlexDirection.COLUMN

        style.setMargin(5f, 5f, 5f, 5f)
        style.position = Style.PositionType.RELATIVE
        style.background.color = ColorConstants.darkGray()
        isFocusable = false

        val sizeX = 170f
        setSizeLimits(sizeX, 260f)

        addFramePanel(sizeX)
        addNodePanel(sizeX)
    }

    private fun addFramePanel(sizeX: Float) {
        val framePanel = Panel()
        framePanel.setSizeLimits(sizeX, 90f)
        framePanel.style.background.color = ColorConstants.darkGray()
        framePanel.style.border.isEnabled = false
        add(framePanel)

        val frameTitle = Label("Keyframe Editor", 0f, 0f, sizeX, 16f)
        frameTitle.style.background.color = Colour.GRAY.rgba
        frameTitle.textState.horizontalAlign = HorizontalAlign.CENTER
        framePanel.add(frameTitle)

        selectedFrame = Label("Selected: N/A", 0f, 20f, sizeX, 15f)
        selectedFrame.textState.horizontalAlign = HorizontalAlign.CENTER
        framePanel.add(selectedFrame)

        val length = Label("Length:", 35f, 40f, 50f, 15f)
        framePanel.add(length)

        frameLength = TextSlider(
            { context.animationHandler.executeCommand(ChangeLengthCommand(context, it)) },
            1 to 99, 81f, 40f, 51f, 15f
        )
        framePanel.add(frameLength)

        val actions = ButtonGroup(Vector2f(23f, 59f), Vector2f(23f, 23f))
        KeyframeAction.values().forEach {
            val button = actions.addButton(it.icon, it.toString())
            button.hoveredIcon = it.hoveredIcon
            button.listenerMap.addListener(MouseClickEvent::class.java) { event ->
                if (event.button == Mouse.MouseButton.MOUSE_BUTTON_LEFT &&
                    event.action == MouseClickEvent.MouseClickAction.CLICK) {
                    it.apply(context)
                }
            }
        }
        actions.style.background.color = ColorConstants.transparent()
        actions.position = Vector2f(23f, 59f)
        actions.setSizes()
        framePanel.add(actions)
    }

    private fun addNodePanel(sizeX: Float) {
        val nodePanel = Panel()
        nodePanel.setSizeLimits(sizeX, 155f)
        nodePanel.style.setMarginTop(95f)
        nodePanel.style.background.color = ColorConstants.darkGray()
        nodePanel.style.border.isEnabled = false
        add(nodePanel)

        val nodeTitle = Label("Node Transformer", 0f, 0f, sizeX, 16f)
        nodeTitle.style.background.color = Colour.GRAY.rgba
        nodeTitle.textState.horizontalAlign = HorizontalAlign.CENTER
        nodePanel.add(nodeTitle)

        selectedNode = Label("Selected: N/A", 0f, 20f, sizeX, 15f)
        selectedNode.textState.horizontalAlign = HorizontalAlign.CENTER
        nodePanel.add(selectedNode)

        transformations = ConfigGroup(
            Vector2f(), Vector2f(24f, 24f),
            arrayOf(ResourceMap["translation"], ResourceMap["rotation"], ResourceMap["scale"]),
            arrayOf("Translation", "Rotation", "Scale"),
            padding = 8
        )
        transformations.position = Vector2f(32f, 42f)

        for ((i, button) in transformations.buttons.withIndex()) {
            button.listenerMap.addListener(MouseClickEvent::class.java) { event ->
                if (event.button == Mouse.MouseButton.MOUSE_BUTTON_LEFT &&
                    event.action == MouseClickEvent.MouseClickAction.CLICK) {
                    val type = TransformationType.fromId(i + 1)
                    type?.let(::updateType)
                }
            }
        }
        nodePanel.add(transformations)

        val transformPanel = Panel(32f, 82f, 106f, 70f)
        transformPanel.style.background.color = ColorUtil.fromInt(71, 71, 71, 1f)
        transformPanel.style.border.isEnabled = false
        nodePanel.add(transformPanel)

        var y = 7f
        val coords = arrayOf("X", "Y", "Z")

        for ((i, coord) in coords.withIndex()) {
            val label = Label(coord, 12f, y, 50f, 15f)
            transformPanel.add(label)

            val slider = TextSlider(
                { context.animationHandler.executeCommand(TransformNodeCommand(context, coordIndex = i, value = it)) },
                -255 to 255, 35f, y, 60f, 15f
            )
            sliders.add(slider)
            transformPanel.add(slider)
            y += 20
        }
    }

    fun setKeyframe(keyframe: Keyframe) {
        selectedFrame.textState.text = "Selected: ${keyframe.id}"
        frameLength.setLimitedValue(keyframe.length)
    }

    fun setNode(node: ReferenceNode, selectedType: TransformationType) {
        for ((i, button) in transformations.buttons.withIndex()) {
            val type = TransformationType.fromId(i + 1)?: continue
            button.isFocusable = node.getTransformation(type) != null
        }
        transformations.updateConfigs(transformations.buttons[selectedType.id - 1])

        selectedNode.textState.text = "Selected: ${node.id}"
        updateType(selectedType)
    }

    private fun updateType(type: TransformationType) {
        if (type != TransformationType.REFERENCE) {
            context.nodeRenderer.updateType(type)
            val transformation = context.nodeRenderer.selectedNode?.getTransformation(type) ?: return
            repeat(sliders.size) {
                sliders[it].setLimitedValue(transformation.delta[it])
            }
        }
    }

    fun reset() {
        val unselected = "Selected: N/A"
        selectedFrame.textState.text = unselected
        selectedNode.textState.text = unselected
        frameLength.setLimitedValue(0)
        sliders.forEach { it.setLimitedValue(0) }
    }
}