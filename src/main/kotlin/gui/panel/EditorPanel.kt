package gui.panel

import Processor
import animation.Reference
import animation.TransformationType
import animation.node.ReferenceNode
import gui.Gui
import gui.component.TextSlider
import org.joml.Vector2f
import org.liquidengine.legui.component.Label
import org.liquidengine.legui.component.Panel
import org.liquidengine.legui.component.SelectBox
import org.liquidengine.legui.component.optional.align.HorizontalAlign
import org.liquidengine.legui.style.color.ColorConstants

class EditorPanel(private val gui: Gui, private val context: Processor): Panel() {

    private val sliders = ArrayList<TextSlider>()
    private val selectBox = SelectBox<String>(44f, 40f, 82f, 15f)
    private var currentReference: Reference? = null

    init {
        position = getPanelPosition()
        size = getPanelSize()
        style.background.color = ColorConstants.darkGray()
        isFocusable = false

        val title = Label("Keyframe Editor", 0f, 5f, size.x, 15f)
        title.textState.horizontalAlign = HorizontalAlign.CENTER
        add(title)

        val types = TransformationType.values()
        selectBox.visibleCount = types.size
        types.forEach { selectBox.addElement(it.toString()) }
        selectBox.addSelectBoxChangeSelectionEventListener { event ->
            for (type in types) {
                if (event.newValue.toString() == type.toString()) {
                    context.framebuffer.nodeRenderer.selectedType = type
                    updateSliders(type)
                    break
                }
            }
        }
        add(selectBox)

        var y = 65f
        val coords = arrayOf("X", "Y", "Z")

        for ((i, coord) in coords.withIndex()){
            val label = Label(coord, 19f, y, 50f, 15f)
            add(label)

            val slider = TextSlider(context, i, 44f, y, 82f, 15f)
            sliders.add(slider)
            add(slider)
            y += 20
        }
    }

    fun setNode(node: ReferenceNode, selectedType: TransformationType) {
        for (i in selectBox.elements.size - 1 downTo 0) {
            selectBox.removeElement(i)
        }

        node.reference.group.forEach {
            selectBox.addElement(it.value.type.toString())
        }
        selectBox.visibleCount = node.reference.group.size
        selectBox.setSelected(selectedType.toString(), true)

        currentReference = node.reference
        updateSliders(selectedType)
    }

    private fun updateSliders(type: TransformationType) {
        val transformation = currentReference!!.group[type]!!
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