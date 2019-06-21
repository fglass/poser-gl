package gui.panel

import RESOURCES_PATH
import gui.Gui
import gui.component.ImageButton
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.joml.Vector2f
import org.liquidengine.legui.component.Label
import org.liquidengine.legui.component.Panel
import org.liquidengine.legui.component.TextInput
import org.liquidengine.legui.component.optional.align.HorizontalAlign
import org.liquidengine.legui.event.CursorEnterEvent
import org.liquidengine.legui.event.KeyEvent
import org.liquidengine.legui.event.MouseClickEvent
import org.liquidengine.legui.image.BufferedImage
import org.liquidengine.legui.listener.CursorEnterEventListener
import org.liquidengine.legui.listener.MouseClickEventListener
import org.liquidengine.legui.style.color.ColorConstants
import org.lwjgl.glfw.GLFW

class EditPanel(private val gui: Gui): Panel() {

    init {
        position = getPanelPosition()
        size = getPanelSize()
        style.background.color = ColorConstants.darkGray()
        isFocusable = false

        val title = Label("Keyframe Editor", 0f, 5f, size.x, 15f)
        title.textState.horizontalAlign = HorizontalAlign.CENTER
        add(title)

        val sliders = arrayOf("Translation", "Rotation", "Scale")
        val coords = arrayOf("X", "Y", "Z")
        var y = 27f

        for (name in sliders) {
            for (coord in coords){
                val label = Label("$name $coord", 27f, y, 50f, 15f)
                label.textState.horizontalAlign = HorizontalAlign.RIGHT
                add(label)

                val slider = TextSlider(93f, y, 70f, 15f)
                add(slider)
                y += 20
            }
            y += 5
        }
    }

    fun resize() {
        position = getPanelPosition()
        size = getPanelSize()
    }

    private fun getPanelPosition(): Vector2f {
        return Vector2f(gui.size.x - 175, gui.size.y - 345)
    }

    private fun getPanelSize(): Vector2f {
        return Vector2f(170f, 222f)
    }

    class TextSlider(x: Float, y: Float, width: Float, height: Float): Panel(x, y, width, height) {

        private val value = TextInput("0", 12f, 0f, width - 24, height)
        private var adjusting = false
        private val leftArrow = BufferedImage(RESOURCES_PATH + "left.png")
        private val rightArrow = BufferedImage(RESOURCES_PATH + "right.png")

        init {
            isFocusable = false
            value.textState.horizontalAlign = HorizontalAlign.CENTER
            value.style.setBorderRadius(0f)
            value.style.focusedStrokeColor = null
            value.listenerMap.addListener(KeyEvent::class.java) { event ->
                if (event.action == GLFW.GLFW_RELEASE) {
                    val current = value.textState.text.toIntOrNull()
                    if (current != null) {
                        val limited = limitValue(current)
                        if (current != limited) {
                            value.textState.text = limited.toString()
                        }
                    }
                }
            }
            add(value)

            val right = ImageButton(Vector2f(width - 10, 3f), rightArrow)
            right.size = Vector2f(10f, 10f)
            right.listenerMap.addListener(MouseClickEvent::class.java, getClickListener(true))
            right.listenerMap.addListener(CursorEnterEvent::class.java, getCursorListener())
            add(right)

            val left = ImageButton(Vector2f(0f, 3f), leftArrow)
            left.size = Vector2f(10f, 10f)
            left.listenerMap.addListener(MouseClickEvent::class.java, getClickListener(false))
            left.listenerMap.addListener(CursorEnterEvent::class.java, getCursorListener())
            add(left)
        }

        private fun getClickListener(increment: Boolean): MouseClickEventListener {
            return MouseClickEventListener { event ->
                when {
                    event.action == MouseClickEvent.MouseClickAction.PRESS -> {
                        adjusting = true
                        GlobalScope.launch {
                            while (adjusting) {
                                adjustValue(increment)
                                delay(100)
                            }
                        }
                    }
                    else -> adjusting = false
                }
            }
        }

        private fun getCursorListener(): CursorEnterEventListener {
            return CursorEnterEventListener { event->
                if (!event.isEntered) {
                    adjusting = false
                }
            }
        }

        private fun adjustValue(increment: Boolean) {
            val newValue = value.textState.text.toInt() + if (increment) 1 else -1
            value.textState.text = limitValue(newValue).toString()
        }

        private fun limitValue(value: Int): Int {
            val limit = 255
            val newValue = Math.min(value, limit)
            return Math.max(newValue, -limit)
        }
    }
}