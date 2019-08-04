package gui.component

import BG_COLOUR
import Processor
import animation.Animation
import org.displee.CacheLibrary
import org.liquidengine.legui.component.Label
import org.liquidengine.legui.component.ScrollablePanel
import org.liquidengine.legui.component.optional.align.HorizontalAlign

class DatDialog(private val context: Processor, private val animation: Animation):
      Dialog("Sequence Information", "", context, 260f, 80f) {

    private lateinit var framePanel: ScrollablePanel

    init {
        isDraggable = false
        addFrames()
    }

    private fun addFrames() {
        val archiveLabel = Label("Archive:", 10f, 7f, 45f, 15f)
        archiveLabel.textState.horizontalAlign = HorizontalAlign.RIGHT
        container.add(archiveLabel)

        val archiveId = if (animation.modified) {
            val library = CacheLibrary(context.cacheService.cachePath)
            val newId = context.cacheService.getMaxFrameArchive(library) + 1
            library.close()
            newId
        } else {
            animation.sequence.frameIDs.first() ushr 16
        }

        val archiveSlider = TextSlider({ setFrames(it) }, Pair(0, 9999), 66f, 7f, 65f, 15f)
        archiveSlider.setValue(archiveId)
        container.add(archiveSlider)

        val frameLabel = Label("Frames:", 10f, 27f, 45f, 15f)
        frameLabel.textState.horizontalAlign = HorizontalAlign.RIGHT
        container.add(frameLabel)

        framePanel = ScrollablePanel(66f, 27f, 177f, 26f)
        framePanel.remove(framePanel.verticalScrollBar)
        framePanel.horizontalScrollBar.style.focusedStrokeColor = null
        framePanel.horizontalScrollBar.style.setRight(0f)
        framePanel.viewport.style.setRight(0f)
        framePanel.container.style.focusedStrokeColor = null
        framePanel.style.background.color = BG_COLOUR
        container.add(framePanel)
        setFrames(archiveId)
    }

    private fun setFrames(archiveId: Int) {
        val sequence = if (animation.modified) animation.toSequence(archiveId) else animation.sequence
        val list = sequence.frameIDs.joinToString()
        val charWidth = 6.5f
        val width = list.length * charWidth
        val frameList = Label(list, 2f, 1f, width, 15f)

        framePanel.container.size.x = width
        framePanel.container.clearChildComponents()
        framePanel.container.add(frameList)
    }
}