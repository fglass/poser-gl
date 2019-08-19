package gui.component

import render.SPRITE_PATH
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.joml.Vector2f
import org.liquidengine.legui.component.Panel
import org.liquidengine.legui.component.TextInput
import org.liquidengine.legui.component.optional.align.HorizontalAlign
import org.liquidengine.legui.event.CursorEnterEvent
import org.liquidengine.legui.event.KeyEvent
import org.liquidengine.legui.event.MouseClickEvent
import org.liquidengine.legui.image.BufferedImage
import org.liquidengine.legui.listener.CursorEnterEventListener
import org.liquidengine.legui.listener.MouseClickEventListener
import org.lwjgl.glfw.GLFW
import kotlin.math.max
import kotlin.math.min

class TextSlider(private val onValueChange: (Int) -> Unit, private val limits: Pair<Int, Int>,
                 x: Float, y: Float, width: Float, height: Float): Panel(x, y, width, height) {

    private val value = TextInput("", 12f, 0f, width - 24, height)
    private var adjusting = false
    private val leftArrow = BufferedImage(SPRITE_PATH + "left.png")
    private val rightArrow = BufferedImage(SPRITE_PATH + "right.png")

    init {
        isFocusable = false
        value.textState.horizontalAlign = HorizontalAlign.CENTER
        value.style.setBorderRadius(0f)
        value.style.focusedStrokeColor = null
        value.listenerMap.addListener(KeyEvent::class.java) { event ->
            if (event.action == GLFW.GLFW_RELEASE) {
                val current = getValue()?: return@addListener
                val limited = limit(current)
                if (current != limited) { // Only set if different
                    setValue(limited)
                }
                onValueChange(limited)
            }
        }
        setLimitedValue(0)
        add(value)

        val right = ImageButton(Vector2f(width - 10, 3f), rightArrow)
        val left = ImageButton(Vector2f(0f, 3f), leftArrow)
        val arrows = arrayOf(left, right)

        arrows.forEach {
            it.size = Vector2f(10f, 10f)
            it.listenerMap.addListener(MouseClickEvent::class.java, getClickListener(it == right))
            it.listenerMap.addListener(CursorEnterEvent::class.java, getCursorListener())
            add(it)
        }
    }

    private fun getClickListener(increment: Boolean): MouseClickEventListener {
        return MouseClickEventListener { event ->
            when {
                event.action == MouseClickEvent.MouseClickAction.PRESS -> {
                    adjusting = true
                    GlobalScope.launch {
                        while (adjusting) {
                            adjust(if (increment) 1 else - 1)
                            delay(10)
                        }
                    }
                }
                else -> adjusting = false
            }
        }
    }

    private fun getCursorListener() = CursorEnterEventListener { event ->
        if (!event.isEntered) {
            adjusting = false
        }
    }

    fun getValue(): Int? {
        return value.textState.text.toIntOrNull()
    }

    fun setValue(newValue: Int) {
        value.textState.text = newValue.toString()
    }

    fun setLimitedValue(newValue: Int) {
        val limited = limit(newValue)
        setValue(limited)
    }

    private fun limit(value: Int): Int {
        val newValue = min(value, limits.second)
        return max(newValue, limits.first)
    }

    fun adjust(delta: Int) {
        val newValue = value.textState.text.toInt() + delta
        val limited = limit(newValue)
        setValue(limited)
        onValueChange(limited)
    }
}