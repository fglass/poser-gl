package gui

import RESOURCES_PATH
import net.runelite.cache.definitions.SequenceDefinition
import org.joml.Vector2f
import org.liquidengine.legui.component.ImageView
import org.liquidengine.legui.component.Label
import org.liquidengine.legui.component.Panel
import org.liquidengine.legui.component.optional.align.HorizontalAlign
import org.liquidengine.legui.image.BufferedImage
import org.liquidengine.legui.style.color.ColorConstants

class AnimationPanel(private val gui: Gui): Panel() {

    private val animationId: Label
    private val timeline: Panel
    private val times: Panel
    private var maxLength = 30
    private var unitX = 0f

    private val greyLine = BufferedImage(RESOURCES_PATH + "grey-line.png")
    private val yellowLine = BufferedImage(RESOURCES_PATH + "yellow-line.png")
    private val greenLine = BufferedImage(RESOURCES_PATH + "green-line.png")
    private val cursor = ImageView(greenLine)
    private var sequence = SequenceDefinition(-1)

    init {
        resize()
        style.background.color = ColorConstants.darkGray()

        val menu = Panel(0f, 0f, size.x, 19f)
        menu.style.background.color = ColorConstants.darkGray()
        menu.style.setBorderRadius(0f)
        menu.style.border.isEnabled = false
        add(menu)

        val x = 12f
        val animation = Label("Animation:", x, 2f, 100f, 15f)
        animationId = Label("N/A", x + 62, 2f, 104f, 15f)
        menu.add(animation)
        menu.add(animationId)

        timeline = Panel(x, 20f, getTimelineWidth() + 1, 65f)
        timeline.style.background.color = ColorConstants.darkGray()
        timeline.style.setBorderRadius(0f)
        add(timeline)

        times = Panel(0f, 86f, size.x, 15f)
        times.style.background.color = ColorConstants.darkGray()
        times.style.border.isEnabled = false
        add(times)

        unitX = getUnitX()
        addTimes()
    }

    fun setTimeline(sequence: SequenceDefinition) {
        timeline.removeAll(timeline.childComponents)
        times.removeAll(times.childComponents)
        this.sequence = sequence

        if (sequence.id == -1) {
            animationId.textState.text = "N/A"
            return
        }

        animationId.textState.text = sequence.id.toString()
        maxLength = sequence.frameLenghts.sum()
        unitX = getUnitX()

        addTimes()
        addKeyframes(sequence)
        addCursor()
    }

    private fun addTimes() {
        addTime(maxLength)
        for (i in 0..maxLength step 5) {
            addTime(i)
        }
    }

    private fun addTime(i: Int) {
        val offsetX = 12f
        val x = i * unitX
        val time = Label(i.toString(), x + offsetX, 0f, 1f, 15f)
        time.textState.horizontalAlign = HorizontalAlign.CENTER
        times.add(time)

        val marker = ImageView(if (i == 0) yellowLine else greyLine)
        marker.position = Vector2f(x, 0f)
        marker.size = Vector2f(1f, 65f)
        marker.style.setBorderRadius(0f)
        marker.style.border.isEnabled = false
        timeline.add(marker)
    }

    private fun addKeyframes(sequence: SequenceDefinition) {
        var cumulative = 0f
        for (i in 1 until sequence.frameLenghts.size) {
            val previous = sequence.frameLenghts[i - 1]
            cumulative += previous
            val x = cumulative * unitX

            val keyframe = ImageView(yellowLine)
            keyframe.position = Vector2f(x, 0f)
            keyframe.size = Vector2f(1f, 65f)
            keyframe.style.setBorderRadius(0f)
            keyframe.style.border.isEnabled = false
            timeline.add(keyframe)
        }
    }

    private fun addCursor() {
        cursor.position = Vector2f(0f, 0f)
        cursor.size = Vector2f(2f, 65f)
        cursor.style.setBorderRadius(0f)
        cursor.style.border.isEnabled = false
        timeline.add(cursor)
    }

    fun tickCursor(timer: Int) {
        val unit = (timeline.size.x - 1) / maxLength
        cursor.position.x = timer * unit
    }

    fun resize() {
        position = getPanelPosition()
        size = getPanelSize()
        if (timeline != null) {
            times.size.x = size.x
            timeline.size.x = getTimelineWidth()
            unitX = getUnitX()
            setTimeline(sequence)
        }
    }

    private fun getPanelPosition(): Vector2f {
        return Vector2f(5f, gui.size.y - 105)
    }

    private fun getPanelSize(): Vector2f {
        return Vector2f(gui.size.x - 10, 100f)
    }

    private fun getTimelineWidth(): Float {
        return size.x - 2 * 12 - 1
    }

    private fun getUnitX(): Float {
        return (timeline.size.x - 1) / maxLength
    }
}