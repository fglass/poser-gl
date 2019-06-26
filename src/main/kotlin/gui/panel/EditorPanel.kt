package gui.panel

import Processor
import animation.Transformation
import animation.TransformationType
import animation.reference.Node
import gui.Gui
import gui.component.TextSlider
import org.joml.Vector2f
import org.liquidengine.legui.component.Label
import org.liquidengine.legui.component.Panel
import org.liquidengine.legui.component.optional.align.HorizontalAlign
import org.liquidengine.legui.style.color.ColorConstants

class EditorPanel(private val gui: Gui, private val context: Processor): Panel() {

    private val sliders = HashMap<TransformationType, ArrayList<TextSlider>>()

    init {
        position = getPanelPosition()
        size = getPanelSize()
        style.background.color = ColorConstants.darkGray()
        isFocusable = false

        val title = Label("Keyframe Editor", 0f, 5f, size.x, 15f)
        title.textState.horizontalAlign = HorizontalAlign.CENTER
        add(title)

        val types = TransformationType.values()
        val coords = arrayOf("X", "Y", "Z")
        var y = 40f

        for (type in types) {
            sliders[type] = ArrayList()
            for ((i, coord) in coords.withIndex()){
                val label = Label("$type $coord", 5f, y, 72f, 15f)
                label.textState.horizontalAlign = HorizontalAlign.RIGHT
                add(label)

                val slider = TextSlider(context, type, i, 94f, y, 65f, 15f)
                sliders[type]?.add(slider)
                add(slider)
                y += 20
            }
            y += 3
        }
    }

    fun setNode(node: Node) {
        setSlider(node.reference)
        node.reference.children.forEach { setSlider(it) }
    }

    private fun setSlider(tf: Transformation) { // TODO: Clean-up
        val slider = sliders[tf.type]?: return
        slider[0].value.textState.text = tf.offset.x.toString()
        slider[1].value.textState.text = tf.offset.y.toString()
        slider[2].value.textState.text = tf.offset.z.toString()
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