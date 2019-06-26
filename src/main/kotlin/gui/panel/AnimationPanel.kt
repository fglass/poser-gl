package gui.panel

import BG_COLOUR
import RESOURCES_PATH
import Processor
import animation.MAX_LENGTH
import gui.Gui
import gui.component.HoverButton
import gui.component.ImageButton
import net.runelite.cache.definitions.SequenceDefinition
import org.joml.Vector2f
import org.joml.Vector4f
import org.liquidengine.legui.component.CheckBox
import org.liquidengine.legui.component.Label
import org.liquidengine.legui.component.Panel
import org.liquidengine.legui.component.optional.align.HorizontalAlign
import org.liquidengine.legui.event.MouseClickEvent
import org.liquidengine.legui.image.BufferedImage
import org.liquidengine.legui.style.color.ColorConstants
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

class AnimationPanel(private val gui: Gui, private val context: Processor): Panel() {

    private val sequenceId: Label
    private val play: ImageButton
    private val menu: Panel
    private val nodeToggle: CheckBox
    private val timeline: Panel
    private val times: Panel
    private var unitX = 0f

    private val playIcon = BufferedImage(RESOURCES_PATH + "play.png")
    private val pauseIcon = BufferedImage(RESOURCES_PATH + "pause.png")
    private val greyLine = BufferedImage(RESOURCES_PATH + "grey-line.png")
    private val yellowLine = BufferedImage(RESOURCES_PATH + "yellow-line.png")
    private val pinkLine = BufferedImage(RESOURCES_PATH + "pink-line.png")
    private val greenLine = BufferedImage(RESOURCES_PATH + "green-line.png")
    private val cursor = ImageButton(Vector2f(0f, 0f), greenLine)
    private var sequence = SequenceDefinition(-1)

    init {
        val x = 12f
        timeline = Panel(x, 24f, getTimelineWidth(), 65f)
        timeline.style.background.color = Vector4f(BG_COLOUR, BG_COLOUR, BG_COLOUR, 1f)
        timeline.style.setBorderRadius(0f)
        timeline.style.focusedStrokeColor = null
        timeline.listenerMap.addListener(MouseClickEvent::class.java) { event ->
            if (event.action == MouseClickEvent.MouseClickAction.CLICK) {
                adjustTime(event.position.x)
            }
        }
        add(timeline)

        times = Panel(0f, 90f, size.x, 15f)
        times.style.background.color = ColorConstants.darkGray()
        times.style.border.isEnabled = false
        add(times)

        style.background.color = ColorConstants.darkGray()
        isFocusable = false
        resize()

        menu = Panel(0f, 0f, size.x, 19f)
        menu.style.background.color = ColorConstants.darkGray()
        menu.style.setBorderRadius(0f)
        menu.style.border.isEnabled = false
        add(menu)

        play = ImageButton(Vector2f(x, 6f), playIcon)
        play.listenerMap.addListener(MouseClickEvent::class.java) { event ->
            if (event.action == MouseClickEvent.MouseClickAction.CLICK) {
                if (sequence.id != -1) {
                    context.animationHandler.playing = !context.animationHandler.playing
                    play.image = if (context.animationHandler.playing) pauseIcon else playIcon
                }
            }
        }
        play.size = Vector2f(10f, 10f)
        menu.add(play)

        val animation = Label("Sequence:", x + 14, 3f, 100f, 15f)
        sequenceId = Label("N/A", x + 74, 3f, 104f, 15f)
        menu.add(animation)
        menu.add(sequenceId)

        nodeToggle = CheckBox("Nodes", size.x - 61, 3f, 49f, 15f)
        nodeToggle.style.focusedStrokeColor = null
        nodeToggle.textState.horizontalAlign = HorizontalAlign.RIGHT
        nodeToggle.listenerMap.addListener(MouseClickEvent::class.java) {
            context.framebuffer.nodeRenderer.enabled = !context.framebuffer.nodeRenderer.enabled
        }
        menu.add(nodeToggle)
    }

    fun play(sequence: SequenceDefinition) {
        this.sequence = sequence
        play.image = pauseIcon
        setTimeline()
    }

    fun stop() {
        play.image = playIcon
        sequence = SequenceDefinition(-1)
        setTimeline()
    }

    private fun setTimeline() {
        timeline.removeAll(timeline.childComponents)
        times.removeAll(times.childComponents)

        if (sequence.id == -1) {
            sequenceId.textState.text = "N/A"
            return
        }

        sequenceId.textState.text = sequence.id.toString()
        unitX = getUnitX()

        addTimes()
        addKeyframes(sequence)
        cursor.position.x = 0f
        timeline.add(cursor)
    }

    private fun addTimes() {
        val max = getMaxLength()
        addTime(max)
        val sqrt = sqrt(max.toDouble())
        val timeStep = 5 * ((sqrt / 5).roundToInt())
        for (i in 0 until max step timeStep) {
            addTime(i)
        }
    }

    private fun addTime(i: Int) {
        val offsetX = 12f
        val x = i * unitX
        val time = Label(i.toString(), x + offsetX, 0f, 1f, 15f)
        time.textState.horizontalAlign = HorizontalAlign.CENTER
        times.add(time)

        if (i > 0) {
            val marker = ImageButton(Vector2f(x, 0f), greyLine)
            timeline.add(marker)
        }
    }

    private fun addKeyframes(sequence: SequenceDefinition) {
        var cumulative = 0
        for (i in 0 until sequence.frameLenghts.size) {
            val previous = if (i > 0) sequence.frameLenghts[i - 1] else 0
            cumulative += previous

            val x = cumulative * unitX
            val keyframe = HoverButton(Vector2f(x, 0f), 2f, yellowLine, pinkLine)
            keyframe.listenerMap.addListener(MouseClickEvent::class.java) { event ->
                if (event.action == MouseClickEvent.MouseClickAction.CLICK) {
                    adjustTime(x)
                }
            }
            timeline.add(keyframe)
        }
    }

    fun tickCursor(timer: Int) {
        cursor.position.x = timer * getUnitX()
    }

    private fun adjustTime(x: Float) {
        val timer = (x / unitX).roundToInt()
        var cumulative = 0

        for ((frameCount, length) in sequence.frameLenghts.withIndex()) {
            if (timer >= cumulative && timer < cumulative + length) {
                context.animationHandler.setFrame(timer, frameCount, timer - cumulative)
                return
            }
            cumulative += length
        }
    }

    fun resize() {
        position = getPanelPosition()
        size = getPanelSize()
        times.size.x = size.x
        timeline.size.x = getTimelineWidth()
        if (menu != null) {
            menu.size.x = size.x
            nodeToggle.position.x = size.x - 61
        }
        if (sequence.id != -1) {
            unitX = getUnitX()
            setTimeline()
        }
    }

    private fun getPanelPosition(): Vector2f {
        return Vector2f(5f, gui.size.y - 117)
    }

    private fun getPanelSize(): Vector2f {
        return Vector2f(gui.size.x - 10, 112f)
    }

    private fun getTimelineWidth(): Float {
        return size.x - 2 * 12 - 1
    }

    private fun getUnitX(): Float {
        return timeline.size.x / getMaxLength()
    }

    private fun getMaxLength(): Int {
        return min(sequence.frameLenghts.sum(), MAX_LENGTH)
    }
}