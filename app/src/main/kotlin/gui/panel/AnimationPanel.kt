package gui.panel

import render.RenderContext
import animation.Keyframe
import gui.component.AnimationMenu
import gui.component.AnimationTimeline
import gui.component.ImageButton
import org.joml.Vector2f
import org.liquidengine.legui.component.Label
import org.liquidengine.legui.component.Panel
import org.liquidengine.legui.component.optional.align.HorizontalAlign
import org.liquidengine.legui.event.CursorEnterEvent
import org.liquidengine.legui.event.MouseClickEvent
import org.liquidengine.legui.image.BufferedImage
import org.liquidengine.legui.style.Style
import org.liquidengine.legui.style.color.ColorConstants
import org.liquidengine.legui.style.flex.FlexStyle
import util.Colour
import util.ResourceMap
import util.setHeightLimit
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sqrt

class AnimationPanel(private val context: RenderContext): Panel() {

    val menu: AnimationMenu
    private val timeline: AnimationTimeline
    private val times: Panel
    private var unitX = 0f

    private val greyLine = ResourceMap["grey-line"]
    private val yellowLine = ResourceMap["yellow-line"]
    private val pinkLine = ResourceMap["pink-line"]
    private val greenLine = ResourceMap["green-line"]
    private val blueLine = ResourceMap["blue-line"]
    private val redLine = ResourceMap["red-line"]
    private val cursor = ImageButton(Vector2f(), getCursorColour(context.settingsManager.cursorColour))

    init {
        style.background.color = ColorConstants.darkGray()
        isFocusable = false

        style.display = Style.DisplayType.FLEX
        style.flexStyle.flexDirection = FlexStyle.FlexDirection.COLUMN
        style.setMargin(0f, 5f, 5f, 5f)
        style.position = Style.PositionType.RELATIVE
        setHeightLimit(112f)
        style.flexStyle.flexGrow = 1

        menu = AnimationMenu(context)
        add(menu)

        timeline = AnimationTimeline(this)
        add(timeline)

        times = Panel()
        times.style.position = Style.PositionType.RELATIVE
        times.style.setMarginTop(3f)
        times.setHeightLimit(15f)
        times.style.flexStyle.flexGrow = 1
        times.style.background.color = ColorConstants.darkGray()
        times.style.border.isEnabled = false
        add(times)

        reset()
    }

    fun reset() {
        menu.updatePlayIcon(false)
        setTimeline()
        setDefaultLayout()
    }

    private fun setDefaultLayout() {
        val placeholder = 50
        unitX = timeline.getUnitX(placeholder)
        addTimes(placeholder)
    }

    fun setTimeline() {
        timeline.clearChildComponents()
        times.clearChildComponents()

        val animation = context.animationHandler.currentAnimation
        if (animation == null) {
            setDefaultLayout()
            return
        }

        unitX = timeline.getUnitX(animation.length)
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
        val xOffset = 12f
        val x = i * unitX
        val time = Label(i.toString(), x + xOffset, 0f, 1f, 15f)
        time.textState.horizontalAlign = HorizontalAlign.CENTER
        times.add(time)

        if (i != 0) {
            val marker = ImageButton(Vector2f(x, 0f), greyLine)
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

            val line = ImageButton(Vector2f(border, 0f), yellowLine)
            line.isFocusable = false
            keyframe.add(line)

            keyframe.listenerMap.addListener(CursorEnterEvent::class.java) { event ->
                line.image = if (event.isEntered) pinkLine else yellowLine
            }
            keyframe.listenerMap.addListener(MouseClickEvent::class.java) { event ->
                if (event.action == MouseClickEvent.MouseClickAction.CLICK) {
                    adjustTime(x)
                }
            }
            timeline.add(keyframe)
        }
    }

    fun tickCursor(timer: Int, maxLength: Int) {
        cursor.position.x = timer * timeline.getUnitX(maxLength)
    }

    fun adjustTime(x: Float) {
        val timer = (x / unitX).roundToInt()
        var cumulative = 0

        val animation = context.animationHandler.currentAnimation?: return
        for ((frameCount, frame) in animation.keyframes.withIndex()) {
            if (timer >= cumulative && timer < cumulative + frame.length) {
                context.animationHandler.setCurrentFrame(frameCount, timer - cumulative)
                return
            }
            cumulative += frame.length
        }
    }

    private fun getCursorColour(colour: Colour): BufferedImage {
        return when (colour) {
            Colour.RED -> redLine
            Colour.BLUE -> blueLine
            else -> greenLine
        }
    }

    fun setCursorColour(colour: Colour) {
        cursor.setIconImage(getCursorColour(colour))
    }
}