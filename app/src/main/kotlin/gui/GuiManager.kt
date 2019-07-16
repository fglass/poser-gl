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
import org.liquidengine.legui.style.color.ColorConstants
import org.liquidengine.legui.style.flex.FlexStyle

class GuiManager(frame: Frame, context: Processor) {

    val container: LayerContainer = frame.componentLayer.container
    val listPanel = ListPanel(context)
    val managerPanel = ManagerPanel(this, context)
    val animationPanel = AnimationPanel(this, context)
    val editorPanel = EditorPanel(this, context)

    init {
        container.style.display = Style.DisplayType.FLEX
        container.style.background.color = ColorConstants.lightRed()
        container.style.flexStyle.flexDirection = FlexStyle.FlexDirection.COLUMN

        container.style.setBorderRadius(0f)
        container.style.focusedStrokeColor = null
        container.size = Vector2f(800f, 600f) // TODO remove

        container.add(listPanel)
        //container.add(managerPanel)
        //container.add(editorPanel)
        //container.add(animationPanel)

        /*style.background.color = ColorConstants.lightRed()
        style.display = Style.DisplayType.FLEX

        style.flexStyle.flexGrow = 1
        /*style.flexStyle.alignItems = FlexStyle.AlignItems.STRETCH
        style.flexStyle.flexDirection = FlexStyle.FlexDirection.COLUMN
        style.flexStyle.justifyContent = FlexStyle.JustifyContent.CENTER*/
        style.setMargin(0f, 5f, 0f, 5f)
        style.position = Style.PositionType.RELATIVE

        style.setMinimumSize(size.x, size.y)
        style.setMaximumSize(Float.MAX_VALUE, Float.MAX_VALUE)*/
    }

    fun resize(size: Vector2f) {
        //setSize(size)
        WIDTH = size.x.toInt()
        HEIGHT = size.y.toInt()

        managerPanel.resize()
        editorPanel.resize()
        animationPanel.resize()
    }
}