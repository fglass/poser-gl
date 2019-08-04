package gui.component

import Processor
import render.Framebuffer
import org.joml.Vector2f
import org.liquidengine.legui.component.Dialog
import org.liquidengine.legui.component.Label
import org.liquidengine.legui.component.optional.align.HorizontalAlign
import org.liquidengine.legui.event.WindowSizeEvent

open class Dialog(title: String, text: String, private val context: Processor, width: Float, height: Float):
           Dialog(title, width, height) {

    val message = Label(text, 0f, 15f, size.x, 15f)

    init {
        message.textState.horizontalAlign = HorizontalAlign.CENTER
        container.add(message)
        container.isFocusable = false
        isFocusable = false
        isResizable = false

        listenerMap.addListener(WindowSizeEvent::class.java) { event ->
            if (this is LoadDialog) {
                position = Vector2f((event.width - size.x) / 2f, (event.height - size.y) / 2f)
            }
        }
    }

    fun display() {
        show(context.frame)
        center(context.framebuffer)
        context.framebuffer.activeDialog = this
    }

    fun center(framebuffer: Framebuffer) {
        val x = framebuffer.size.x - size.x
        val y = framebuffer.size.y - size.y
        position = Vector2f(x / 2f + framebuffer.position.x, y / 2f + framebuffer.position.y)
    }
}