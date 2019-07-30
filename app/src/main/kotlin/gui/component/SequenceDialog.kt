package gui.component

import net.runelite.cache.definitions.SequenceDefinition
import org.liquidengine.legui.component.Label
import org.liquidengine.legui.component.TextInput
import org.liquidengine.legui.component.optional.align.HorizontalAlign

class SequenceDialog(private val sequence: SequenceDefinition): Dialog("Sequence ${sequence.id}", "", 260f, 69f) {

    init {
        isDraggable = false
        addAttributes()
    }

    private fun addAttributes() {
        val leftLabel = Label("Left Hand:", 24f, 7f, 45f, 15f)
        leftLabel.textState.horizontalAlign = HorizontalAlign.RIGHT
        leftLabel.style.focusedStrokeColor = null
        container.add(leftLabel)

        val leftHandItem = TextInput(sequence.leftHandItem.toString(), 76f, 7f, 65f, 15f)
        leftHandItem.style.focusedStrokeColor = null
        container.add(leftHandItem)

        val rightLabel = Label("Right Hand:", 24f, 27f, 45f, 15f)
        rightLabel.textState.horizontalAlign = HorizontalAlign.RIGHT
        rightLabel.style.focusedStrokeColor = null
        container.add(rightLabel)

        val rightHandItem = TextInput(sequence.rightHandItem.toString(), 76f, 27f, 65f, 15f)
        rightHandItem.style.focusedStrokeColor = null
        container.add(rightHandItem)
    }
}