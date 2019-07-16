package gui

import HEIGHT
import WIDTH
import Processor
import gui.panel.AnimationPanel
import gui.panel.EditorPanel
import gui.panel.ListPanel
import gui.panel.ManagerPanel
import org.joml.Vector2f
import org.liquidengine.legui.component.Frame
import org.liquidengine.legui.component.LayerContainer
import org.liquidengine.legui.style.Style
import org.liquidengine.legui.style.flex.FlexStyle

class GuiManager(frame: Frame, context: Processor) {

    val container: LayerContainer = frame.componentLayer.container
    val listPanel = ListPanel(context)
    val managerPanel = ManagerPanel(context)
    val animationPanel = AnimationPanel(this, context)
    val editorPanel = EditorPanel(this, context)

    init {
        container.style.display = Style.DisplayType.FLEX
        container.style.flexStyle.flexDirection = FlexStyle.FlexDirection.ROW
        container.style.flexStyle.justifyContent = FlexStyle.JustifyContent.SPACE_BETWEEN

        container.style.setBorderRadius(0f)
        container.style.focusedStrokeColor = null
        container.size = Vector2f(800f, 600f) // TODO remove

        container.add(listPanel)
        container.add(managerPanel)
        //container.add(editorPanel)
        //container.add(animationPanel)
    }

    fun resize(size: Vector2f) {
        WIDTH = size.x.toInt()
        HEIGHT = size.y.toInt()
    }
}