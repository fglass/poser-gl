package gui.panel

import BG_COLOUR
import Processor
import SPRITE_PATH
import animation.Keyframe
import gui.GuiManager
import gui.component.AnimationMenu
import gui.component.ImageButton
import org.joml.Vector2f
import org.liquidengine.legui.component.Label
import org.liquidengine.legui.component.Panel
import org.liquidengine.legui.component.optional.align.HorizontalAlign
import org.liquidengine.legui.event.CursorEnterEvent
import org.liquidengine.legui.event.MouseClickEvent
import org.liquidengine.legui.image.BufferedImage
import org.liquidengine.legui.style.color.ColorConstants
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sqrt

class AnimationPanel(private val gui: GuiManager, private val context: Processor): Panel() {

    val menu: AnimationMenu
    private val timeline: Panel
    private val times: Panel
    private var unitX = 0f

    private val greyLine = BufferedImage(SPRITE_PATH + "grey-line.png")
    private val yellowLine = BufferedImage(SPRITE_PATH + "yellow-line.png")
    private val pinkLine = BufferedImage(SPRITE_PATH + "pink-line.png")
    private val greenLine = BufferedImage(SPRITE_PATH + "green-line.png")
    private val cursor = ImageButton(Vector2f(0f, 0f), greenLine, "")

    init {
        val x = 12f
        timeline = Panel(x, 27f, getTimelineWidth(), 65f)
        timeline.style.background.color = BG_COLOUR
        timeline.style.setBorderRadius(0f)
        timeline.style.focusedStrokeColor = null
        timeline.listenerMap.addListener(MouseClickEvent::class.java) { event ->
            if (event.action == MouseClickEvent.MouseClickAction.CLICK) {
                adjustTime(event.position.x)
            }
        }
        add(timeline)

        times = Panel(0f, 93f, size.x, 15f)
        times.style.background.color = ColorConstants.darkGray()
        times.style.border.isEnabled = false
        add(times)

        style.background.color = ColorConstants.darkGray()
        isFocusable = false
        resize()

        menu = AnimationMenu(context, size.x)
        add(menu)
        reset()
    }

    fun reset() {
        menu.updatePlayIcon(false)
        setTimeline()

        // Set to default layout
        val placeholder = 50
        unitX = getUnitX(placeholder)
        addTimes(placeholder)
    }

    fun setTimeline() {
        timeline.removeAll(timeline.childComponents)
        times.removeAll(times.childComponents)

        val animation = context.animationHandler.currentAnimation
        if (animation == null) {
            menu.sequenceId.textState.text = "N/A"
            return
        }

        menu.sequenceId.textState.text = animation.sequence.id.toString()
        unitX = getUnitX(animation.length)
        addTimes(animation.length)
        addKeyframes(animation.keyframes)
        timeline.add(cursor)
    }

    private fun addTimes(maxLength: Int) {
        addTime(maxLength)
        val base = 5
        val sqrt = max(sqrt(maxLength.toDouble()), base.toDouble())
        val timeStep = base * (sqrt / base).roundToInt()

        for (i in 0 until maxLength step timeStep) {
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
            val marker = ImageButton(Vector2f(x, 0f), greyLine, "")
            timeline.add(marker)
        }
    }

    private fun addKeyframes(keyframes: ArrayList<Keyframe>) {
        var cumulative = 0
        for (i in 0 until keyframes.size) {
            val previous = if (i > 0) keyframes[i - 1].length else 0
            cumulative += previous

            val x = cumulative * unitX
            val border = 2f
            val position = Vector2f(x - border, 0f)
            val keyframe = Panel(position, Vector2f(yellowLine.width + 2 * border, yellowLine.height.toFloat()))

            keyframe.style.background.color = ColorConstants.transparent()
            keyframe.style.border.isEnabled = false
            keyframe.style.focusedStrokeColor = null

            val line = ImageButton(Vector2f(border, 0f), yellowLine, "")
            keyframe.listenerMap.addListener(CursorEnterEvent::class.java) { event ->
                line.image = if (event.isEntered) pinkLine else yellowLine
            }
            keyframe.add(line)

            keyframe.listenerMap.addListener(MouseClickEvent::class.java) { event ->
                if (event.action == MouseClickEvent.MouseClickAction.CLICK) {
                    adjustTime(x)
                }
            }
            timeline.add(keyframe)
        }
    }

    fun tickCursor(timer: Int, maxLength: Int) {
        cursor.position.x = timer * getUnitX(maxLength)
    }

    private fun adjustTime(x: Float) {
        val timer = (x / unitX).roundToInt()
        var cumulative = 0

        val animation = context.animationHandler.currentAnimation?: return
        for ((frameCount, frame) in animation.keyframes.withIndex()) {
            if (timer >= cumulative && timer < cumulative + frame.length) {
                context.animationHandler.setFrame(frameCount, timer - cumulative)
                return
            }
            cumulative += frame.length
        }
    }

    fun resize() {
        position = getPanelPosition()
        size = getPanelSize()
        times.size.x = size.x
        timeline.size.x = getTimelineWidth()

        if (menu == null) { // Not initialised at the start
            return
        }
        //nodeToggle.position.x = size.x - 32
        //packButton.position.x = size.x - 60
        //exportButton.position.x = size.x - 85
        //importButton.position.x = size.x - 110

        val animation = context.animationHandler.currentAnimation
        if (animation == null) {
            reset()
        } else {
            unitX = getUnitX(animation.length)
            setTimeline()
        }
    }

    private fun getPanelPosition(): Vector2f {
        return Vector2f(5f, gui.container.size.y - 116)
    }

    private fun getPanelSize(): Vector2f {
        return Vector2f(gui.container.size.x - 10, 111f)
    }

    private fun getTimelineWidth(): Float {
        return size.x - 2 * 12 - 1
    }

    private fun getUnitX(maxLength: Int): Float {
        return timeline.size.x / maxLength
    }
}