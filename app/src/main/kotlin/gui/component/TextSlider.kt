package gui.component

import org.joml.Vector2f
import org.liquidengine.legui.component.Panel
import org.liquidengine.legui.component.TextInput
import org.liquidengine.legui.component.optional.align.HorizontalAlign
import org.liquidengine.legui.event.CursorEnterEvent
import org.liquidengine.legui.event.FocusEvent
import org.liquidengine.legui.event.MouseClickEvent
import org.liquidengine.legui.listener.CursorEnterEventListener
import org.liquidengine.legui.listener.MouseClickEventListener
import util.ResourceMap

class TextSlider(private val onChange: (Int) -> Unit, private val limits: Pair<Int, Int>,
                 x: Float, y: Float, width: Float, height: Float): Panel(x, y, width, height) {

    private val value = TextInput("", 12f, 0f, width - 24, height)
    private var previous = 0
    private var adjusting = false
    private val leftArrow = ResourceMap["left"]
    private val rightArrow = ResourceMap["right"]

    init {
        isFocusable = false
        value.textState.horizontalAlign = HorizontalAlign.CENTER
        value.style.setBorderRadius(0f)
        value.style.focusedStrokeColor = null

        value.listenerMap.addListener(FocusEvent::class.java) { event ->
            if (!event.isFocused) {
                getValue()?.let {
                    if (it != previous) {
                        val limited = limit(it)
                        setValue(limited)
                        onChange(limited)
                    }
                }
            }
        }

        setValue(0)
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
            if (event.action == MouseClickEvent.MouseClickAction.CLICK) {
                adjust(if (increment) 1 else -1)
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
        previous = newValue
    }

    fun setLimitedValue(newValue: Int) {
        val limited = limit(newValue)
        setValue(limited)
    }

    private fun limit(value: Int, cyclic: Boolean = false): Int {
        return if (!cyclic) {
            value.coerceIn(limits.first, limits.second)
        } else {
            when {
                value > limits.second -> limits.first
                value < limits.first -> limits.second
                else -> value
            }
        }
    }

    fun adjust(delta: Int, cyclic: Boolean = false) {
        val newValue = value.textState.text.toInt() + delta
        val limited = limit(newValue, cyclic)
        setValue(limited)
        onChange(limited)
    }
}