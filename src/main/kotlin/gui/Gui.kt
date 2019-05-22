package gui

import HEIGHT
import org.liquidengine.legui.component.CheckBox
import org.liquidengine.legui.component.Frame
import org.liquidengine.legui.component.Panel
import org.liquidengine.legui.event.MouseClickEvent
import org.liquidengine.legui.theme.Themes
import Processor
import WIDTH

/**
 * @author Fred
 */
class Gui(private val context: Processor) {

    fun createElements(frame: Frame) {
        Themes.setDefaultTheme(Themes.FLAT_DARK)
        Themes.getDefaultTheme().applyAll(frame)

        val wireframeToggle = CheckBox("Wireframe", 20f, 5f, 80f, 24f)
        wireframeToggle.listenerMap.addListener(MouseClickEvent::class.java) {
            context.wireframe = !context.wireframe
        }

        val panel = Panel(0f, 0f, WIDTH.toFloat(), HEIGHT.toFloat())
        panel.add(wireframeToggle)
        frame.container.add(panel)
    }
}