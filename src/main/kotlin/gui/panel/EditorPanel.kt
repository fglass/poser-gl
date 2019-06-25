package gui.panel

import Processor
import animation.AnimationHandler
import gui.Gui
import gui.component.TextSlider
import org.joml.Vector2f
import org.liquidengine.legui.component.Label
import org.liquidengine.legui.component.Panel
import org.liquidengine.legui.component.SelectBox
import org.liquidengine.legui.component.optional.align.HorizontalAlign
import org.liquidengine.legui.style.color.ColorConstants

class EditorPanel(private val gui: Gui, private val context: Processor): Panel() {

    private var selectedNode = Label("N/A", 91f, 49f, 50f, 15f)
    private val sliders = ArrayList<TextSlider>()

    init {
        position = getPanelPosition()
        size = getPanelSize()
        style.background.color = ColorConstants.darkGray()
        isFocusable = false

        val title = Label("Keyframe Editor", 0f, 5f, size.x, 15f)
        title.textState.horizontalAlign = HorizontalAlign.CENTER
        add(title)

        val types = arrayOf("Reference", "Translation", "Rotation", "Scale")
        val selectBox = SelectBox<String>(44f, 27f, 82f, 15f)
        types.forEach { selectBox.addElement(it) }
        selectBox.visibleCount = 4
        selectBox.addSelectBoxChangeSelectionEventListener { event ->
            for ((i, type) in types.withIndex()) {
                if (type == event.newValue.toString()) {
                    context.framebuffer.nodeRenderer.activeType = i
                    break
                }
            }
        }
        add(selectBox)

        val node = Label("Node:", 55f, 49f, 50f, 15f)
        add(node)
        add(selectedNode)

        val transformations = arrayOf("X", "Y", "Z")
        var y = 71f

        for ((i, name) in transformations.withIndex()){
            val label = Label(name, 20f, y, 50f, 15f)
            add(label)

            val slider = TextSlider(context, i, 44f, y, 82f, 15f)
            sliders.add(slider)
            add(slider)
            y += 20
        }
    }

    fun setNode(transformation: AnimationHandler.Transformation) {
        selectedNode.textState.text = transformation.id.toString()
        sliders[0].value.textState.text = transformation.dx.toString()
        sliders[1].value.textState.text = transformation.dy.toString()
        sliders[2].value.textState.text = transformation.dz.toString()
    }

    fun resize() {
        position = getPanelPosition()
        size = getPanelSize()
    }

    private fun getPanelPosition(): Vector2f {
        return Vector2f(gui.size.x - 175, gui.size.y - 345)
    }

    private fun getPanelSize(): Vector2f {
        return Vector2f(170f, 222f)
    }
}