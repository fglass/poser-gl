package gui.component

import BG_COLOUR
import Processor
import animation.Animation
import org.displee.CacheLibrary
import org.liquidengine.legui.component.Label
import org.liquidengine.legui.component.ScrollablePanel
import org.liquidengine.legui.component.TextInput
import org.liquidengine.legui.component.optional.align.HorizontalAlign

class SequenceDialog(private val context: Processor, private val animation: Animation):
      Dialog("Sequence ${animation.sequence.id}", "", 260f, 117f)  {

    init {
        isDraggable = false
        addItems()
        addFrames()
    }

    private fun addItems() {
        val leftLabel = Label("Left Hand:", 22f, 5f, 45f, 15f)
        leftLabel.textState.horizontalAlign = HorizontalAlign.RIGHT
        leftLabel.style.focusedStrokeColor = null
        container.add(leftLabel)

        val leftHandItem = TextInput(animation.sequence.leftHandItem.toString(), 75f, 5f, 65f, 15f)
        leftHandItem.style.focusedStrokeColor = null
        container.add(leftHandItem)

        val rightLabel = Label("Right Hand:", 22f, 25f, 45f, 15f)
        rightLabel.textState.horizontalAlign = HorizontalAlign.RIGHT
        rightLabel.style.focusedStrokeColor = null
        container.add(rightLabel)

        val rightHandItem = TextInput(animation.sequence.rightHandItem.toString(), 75f, 25f, 65f, 15f)
        rightHandItem.style.focusedStrokeColor = null
        container.add(rightHandItem)
    }

    private fun addFrames() {
        val archiveLabel = Label("Archive Id:", 22f, 45f, 45f, 15f)
        archiveLabel.textState.horizontalAlign = HorizontalAlign.RIGHT
        container.add(archiveLabel)

        val id = if (animation.modified) {
            val library = CacheLibrary(context.cacheService.cachePath)
            val newId = context.cacheService.getMaxFrameArchive(library) + 1
            library.close()
            newId
        } else {
            animation.sequence.frameIDs.first() ushr 16
        }

        val archiveId = TextSlider({ }, Pair(0, 9999), 75f, 45f, 65f, 15f)
        archiveId.setValue(id)
        container.add(archiveId)

        val frameLabel = Label("Frame Ids:", 22f, 65f, 45f, 15f)
        frameLabel.textState.horizontalAlign = HorizontalAlign.RIGHT
        container.add(frameLabel)

        val framePanel = ScrollablePanel(75f, 65f, 177f, 26f)
        framePanel.remove(framePanel.verticalScrollBar)
        framePanel.horizontalScrollBar.style.focusedStrokeColor = null
        framePanel.horizontalScrollBar.style.setRight(0f)
        framePanel.viewport.style.setRight(0f)
        framePanel.style.background.color = BG_COLOUR
        container.add(framePanel)

        val sequence = if (animation.modified) animation.toSequence(id) else animation.sequence
        val list = sequence.frameIDs.joinToString()
        val width = list.length * 6.5f
        val frameList = Label(list, 2f, 1f, width, 15f)
        framePanel.container.size.x = width
        framePanel.container.add(frameList)
    }
}