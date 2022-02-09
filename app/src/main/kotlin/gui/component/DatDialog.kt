package gui.component

import render.RenderContext
import animation.Animation
import com.displee.cache.CacheLibrary
import com.spinyowl.legui.component.Label
import com.spinyowl.legui.component.ScrollablePanel
import com.spinyowl.legui.component.optional.align.HorizontalAlign
import com.spinyowl.legui.style.font.TextDirection
import util.Colour

class DatDialog(private val context: RenderContext, private val animation: Animation):
      Dialog("Sequence Information", "", context, 260f, 80f) {

    private lateinit var framePanel: ScrollablePanel

    init {
        isDraggable = false
        addFrames()
    }

    private fun addFrames() {
        val archiveLabel = Label("Archive:", 10f, 7f, 45f, 15f)
        archiveLabel.style.horizontalAlign = HorizontalAlign.RIGHT
        container.add(archiveLabel)

        val archiveId = if (animation.modified) {
            val library = CacheLibrary(context.cacheService.path)
            val lastId = library.index(context.cacheService.loader.frameIndex).last()?.id ?: -1
            library.close()
            lastId + 1
        } else {
            animation.sequence.frameIds.first() ushr 16
        }

        val archiveSlider = TextSlider({ setFrames(it) }, 0 to 999, 66f, 7f, 65f, 15f)
        archiveSlider.setLimitedValue(archiveId)
        container.add(archiveSlider)

        val frameLabel = Label("Frames:", 10f, 27f, 45f, 15f)
        frameLabel.style.horizontalAlign = HorizontalAlign.RIGHT
        container.add(frameLabel)

        framePanel = ScrollablePanel(66f, 27f, 177f, 26f)
        framePanel.remove(framePanel.verticalScrollBar)
        framePanel.horizontalScrollBar.style.focusedStrokeColor = null
        framePanel.horizontalScrollBar.style.setRight(0f)
        framePanel.viewport.style.setRight(0f)
        framePanel.container.style.focusedStrokeColor = null
        framePanel.style.background.color = Colour.GRAY.rgba
        container.add(framePanel)
        setFrames(archiveId)
    }

    private fun setFrames(archiveId: Int) {
        val sequence = if (animation.modified) animation.toSequence(archiveId) else animation.sequence
        val list = sequence.frameIds.joinToString()
        val charWidth = 6.5f
        val width = list.length * charWidth
        val frameList = Label(list, 2f, 1f, width, 15f)

        framePanel.container.size.x = width
        framePanel.container.clearChildComponents()
        framePanel.container.add(frameList)
    }
}