package gui.component

import render.RenderContext
import render.Framebuffer
import org.joml.Vector2f
import com.spinyowl.legui.component.Dialog
import com.spinyowl.legui.component.Label
import com.spinyowl.legui.component.optional.align.HorizontalAlign
import com.spinyowl.legui.event.WindowSizeEvent
import com.spinyowl.legui.style.color.ColorConstants

open class Dialog(title: String, text: String, private val context: RenderContext, width: Float, height: Float) :
           Dialog(title, width, height) {

    val message = Label(text, 0f, 15f, size.x, 15f)

    init {
        message.style.horizontalAlign = HorizontalAlign.CENTER
        titleContainer.style.background.color = ColorConstants.darkGray()
        closeButton.style.focusedStrokeColor = null
        //add(message)
        container.isFocusable = false
        isFocusable = false
        isResizable = false

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