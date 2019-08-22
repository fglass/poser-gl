package gui.component

import org.joml.Vector2f
import org.liquidengine.legui.component.Frame
import org.liquidengine.legui.component.ImageView
import org.liquidengine.legui.component.Label
import org.liquidengine.legui.component.Panel
import org.liquidengine.legui.event.WindowSizeEvent
import org.liquidengine.legui.image.BufferedImage
import render.RenderContext
import render.SPRITE_PATH
import render.VERSION

object StartScreen {

    fun show(context: RenderContext, frame: Frame) {
        val dialog = LoadDialog(context)
        dialog.show(frame)

        val xOffset = 5
        val yOffset = 105
        val title = Panel(dialog.position.x + xOffset, dialog.position.y - yOffset, dialog.size.x - xOffset * 2, 94f)
        //title.style.border.isEnabled = false
        title.listenerMap.addListener(WindowSizeEvent::class.java) {
            title.position = Vector2f(dialog.position.x + xOffset, dialog.position.y - yOffset)
        }
        frame.container.add(title)

        val logo = ImageView(BufferedImage(SPRITE_PATH + "title.png"))
        logo.size = Vector2f(title.size)
        //logo.style.border.isEnabled = false
        title.add(logo)

        val version = Label("v$VERSION")
        version.position = Vector2f(title.size.x - 34, title.size.y - 25)
        title.add(version)
    }
}