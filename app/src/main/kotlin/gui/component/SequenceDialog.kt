package gui.component

import BG_COLOUR
import net.runelite.cache.definitions.SequenceDefinition
import org.liquidengine.legui.component.Label
import org.liquidengine.legui.component.ScrollablePanel
import org.liquidengine.legui.component.TextInput
import org.liquidengine.legui.component.optional.align.HorizontalAlign

class SequenceDialog(private val sequence: SequenceDefinition): Dialog("Sequence ${sequence.id}", "", 260f, 117f)  {

    init {
        isDraggable = false
        addItems()
        addFrames()
    }

    private fun addItems() {
        val leftLabel = Label("Left Item:", 20f, 5f, 45f, 15f)
        leftLabel.textState.horizontalAlign = HorizontalAlign.RIGHT
        leftLabel.style.focusedStrokeColor = null
        container.add(leftLabel)

        val leftHandItem = TextInput(sequence.leftHandItem.toString(), 75f, 5f, 65f, 15f)
        leftHandItem.style.focusedStrokeColor = null
        container.add(leftHandItem)

        val rightLabel = Label("Right Item:", 20f, 25f, 45f, 15f)
        rightLabel.textState.horizontalAlign = HorizontalAlign.RIGHT
        rightLabel.style.focusedStrokeColor = null
        container.add(rightLabel)

        val rightHandItem = TextInput(sequence.rightHandItem.toString(), 75f, 25f, 65f, 15f)
        rightHandItem.style.focusedStrokeColor = null
        container.add(rightHandItem)
    }

    private fun addFrames() {
        val archiveLabel = Label("Archive Id:", 20f, 45f, 45f, 15f)
        archiveLabel.textState.horizontalAlign = HorizontalAlign.RIGHT
        container.add(archiveLabel)

        val archiveId = TextSlider({ }, Pair(0, 9999), 75f, 45f, 65f, 15f)
        container.add(archiveId)

        val frameLabel = Label("Frame Ids:", 20f, 65f, 45f, 15f)
        frameLabel.textState.horizontalAlign = HorizontalAlign.RIGHT
        container.add(frameLabel)

        val framePanel = ScrollablePanel(75f, 65f, 177f, 26f)
        framePanel.remove(framePanel.verticalScrollBar)
        framePanel.horizontalScrollBar.style.focusedStrokeColor = null
        framePanel.horizontalScrollBar.style.setRight(0f)
        framePanel.viewport.style.setRight(0f)
        framePanel.style.background.color = BG_COLOUR
        container.add(framePanel)

        val list = sequence.frameIDs.joinToString()
        val width = list.length * 7f
        val frameList = Label(list, 2f, 1f, width, 15f)
        framePanel.container.size.x = width
        framePanel.container.add(frameList)
    }
}