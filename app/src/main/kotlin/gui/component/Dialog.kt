package gui.component

import render.RenderContext
import render.Framebuffer
import org.joml.Vector2f
import org.liquidengine.legui.component.Dialog
import org.liquidengine.legui.component.Label
import org.liquidengine.legui.component.optional.align.HorizontalAlign
import org.liquidengine.legui.event.WindowSizeEvent
import org.liquidengine.legui.style.color.ColorConstants

open class Dialog(title: String, text: String, private val context: RenderContext, width: Float, height: Float) :
           Dialog(title, width, height) {

    val message = Label(text, 0f, 15f, size.x, 15f)

    init {
        message.textState.horizontalAlign = HorizontalAlign.CENTER
        titleContainer.style.background.color = ColorConstants.darkGray()
        closeButton.style.focusedStrokeColor = null
        container.add(message)
        container.isFocusable = false
        isFocusable = false
        isResizable = false
        isDraggable = false

        listenerMap.addListener(WindowSizeEvent::class.java) { event ->
            if (this is StartDialog) { // Center on overall frame instead
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
        val w = framebuffer.size.x - size.x
        val h = framebuffer.size.y - size.y
        position = Vector2f(w / 2f + framebuffer.position.x, h / 2f + framebuffer.position.y)
    }
}