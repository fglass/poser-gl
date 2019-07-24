package gui

import HEIGHT
import WIDTH
import Processor
import gui.panel.AnimationPanel
import gui.panel.EditorPanel
import gui.panel.ListPanel
import gui.panel.ManagerPanel
import org.liquidengine.legui.component.LayerContainer
import org.liquidengine.legui.component.Panel
import org.liquidengine.legui.event.WindowSizeEvent
import org.liquidengine.legui.style.Style
import org.liquidengine.legui.style.color.ColorConstants
import org.liquidengine.legui.style.flex.FlexStyle

class GuiManager(context: Processor) {

    val container: LayerContainer = context.frame.componentLayer.container
    val listPanel = ListPanel(context)
    val managerPanel = ManagerPanel(context)
    val editorPanel = EditorPanel(context)
    val animationPanel = AnimationPanel(context)

    init {
        container.style.display = Style.DisplayType.FLEX
        container.style.flexStyle.flexDirection = FlexStyle.FlexDirection.COLUMN
        container.style.setBorderRadius(0f)
        container.style.focusedStrokeColor = null

        val topPanel = Panel()
        topPanel.style.display = Style.DisplayType.FLEX
        topPanel.style.flexStyle.flexDirection = FlexStyle.FlexDirection.ROW
        topPanel.style.setBorderRadius(0f)
        topPanel.style.focusedStrokeColor = null
        topPanel.style.border.isEnabled = false
        topPanel.style.position = Style.PositionType.RELATIVE
        topPanel.style.flexStyle.flexGrow = 1

        topPanel.add(listPanel)
        topPanel.add(context.framebuffer)

        val rightPanel = Panel()
        rightPanel.style.display = Style.DisplayType.FLEX
        rightPanel.style.flexStyle.flexDirection = FlexStyle.FlexDirection.COLUMN

        rightPanel.style.position = Style.PositionType.RELATIVE
        rightPanel.style.setMaxWidth(180f)
        rightPanel.style.flexStyle.flexGrow = 1
        rightPanel.style.border.isEnabled = false
        rightPanel.style.background.color = ColorConstants.transparent()

        rightPanel.add(managerPanel)
        rightPanel.add(editorPanel)
        topPanel.add(rightPanel)

        container.add(topPanel)
        container.add(animationPanel)

        container.listenerMap.addListener(WindowSizeEvent::class.java) { event ->
            WIDTH = event.width
            HEIGHT = event.height
        }
    }
}