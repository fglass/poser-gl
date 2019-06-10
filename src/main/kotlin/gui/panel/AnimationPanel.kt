package gui.panel

import Processor
import RESOURCES_PATH
import gui.Gui
import gui.component.ImageButton
import net.runelite.cache.definitions.SequenceDefinition
import org.joml.Vector2f
import org.liquidengine.legui.component.Label
import org.liquidengine.legui.component.Panel
import org.liquidengine.legui.component.optional.align.HorizontalAlign
import org.liquidengine.legui.event.MouseClickEvent
import org.liquidengine.legui.image.BufferedImage
import org.liquidengine.legui.style.color.ColorConstants

class AnimationPanel(private val gui: Gui, private val context: Processor): Panel() {

    private val animationId: Label
    private val play: ImageButton
    private val timeline: Panel
    private val times: Panel
    private var maxLength = 30
    private var unitX = 0f

    private val playIcon = BufferedImage(RESOURCES_PATH + "play.png")
    private val pauseIcon = BufferedImage(RESOURCES_PATH + "pause.png")
    private val greyLine = BufferedImage(RESOURCES_PATH + "grey-line.png")
    private val yellowLine = BufferedImage(RESOURCES_PATH + "yellow-line.png")
    private val greenLine = BufferedImage(RESOURCES_PATH + "green-line.png")
    private val cursor = ImageButton(Vector2f(0f, 0f), greenLine)
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
        play = ImageButton(Vector2f(x, 5f), playIcon)
        play.listenerMap.addListener(MouseClickEvent::class.java) { event ->
            if (event.action == MouseClickEvent.MouseClickAction.CLICK) {
                context.animationHandler.playing = !context.animationHandler.playing
                play.image = if (context.animationHandler.playing) pauseIcon else playIcon
            }
        }
        add(play)

        val animation = Label("Animation:", x + 14, 2f, 100f, 15f)
        animationId = Label("N/A", x + 76, 2f, 104f, 15f)
        menu.add(animation)
        menu.add(animationId)

        timeline = Panel(x, 20f, getTimelineWidth() + 1, 65f)
        timeline.style.background.color = ColorConstants.darkGray()
        timeline.style.setBorderRadius(0f)
        timeline.style.focusedStrokeColor = null
        add(timeline)

        times = Panel(0f, 86f, size.x, 15f)
        times.style.background.color = ColorConstants.darkGray()
        times.style.border.isEnabled = false
        add(times)

        unitX = getUnitX()
        addTimes()
    }

    fun play(sequence: SequenceDefinition) {
        play.image = pauseIcon
        setTimeline(sequence)
    }

    private fun setTimeline(sequence: SequenceDefinition) {
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
        timeline.add(cursor)
    }

    private fun addTimes() {
        addTime(maxLength)
        val sqrt = Math.sqrt(maxLength.toDouble())
        val timeStep = 5 * (Math.round(sqrt / 5))
        for (i in 0..maxLength step timeStep.toInt()) {
            addTime(i)
        }
    }

    private fun addTime(i: Int) {
        val offsetX = 12f
        val x = i * unitX
        val time = Label(i.toString(), x + offsetX, 0f, 1f, 15f)
        time.textState.horizontalAlign = HorizontalAlign.CENTER
        times.add(time)

        val marker = ImageButton(Vector2f(x, 0f), if (i == 0) yellowLine else greyLine)
        timeline.add(marker)
    }

    private fun addKeyframes(sequence: SequenceDefinition) {
        var cumulative = 0f
        for (i in 1 until sequence.frameLenghts.size) {
            val previous = sequence.frameLenghts[i - 1]
            cumulative += previous
            val x = cumulative * unitX

            val keyframe = ImageButton(Vector2f(x, 0f), yellowLine)
            timeline.add(keyframe)
        }
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