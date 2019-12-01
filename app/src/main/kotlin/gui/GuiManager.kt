package gui

import gui.panel.*
import org.liquidengine.legui.component.LayerContainer
import org.liquidengine.legui.component.Panel
import org.liquidengine.legui.style.Style
import org.liquidengine.legui.style.color.ColorConstants
import org.liquidengine.legui.style.flex.FlexStyle
import render.RenderContext

class GuiManager(context: RenderContext) {

    val container: LayerContainer = context.frame.componentLayer.container
    val menuPanel = MenuPanel(context)
    val listPanel = ListPanel(context)
    val managerPanel = ManagerPanel(context)
    val editorPanel = EditorPanel(context)
    val animationPanel = AnimationPanel(context)

    init {
        container.clearChildComponents() // Clear start screen
        container.style.display = Style.DisplayType.FLEX
        container.style.flexStyle.flexDirection = FlexStyle.FlexDirection.COLUMN
        container.style.setBorderRadius(0f)
        container.style.focusedStrokeColor = null

        val mainPanel = Panel()
        mainPanel.style.display = Style.DisplayType.FLEX
        mainPanel.style.flexStyle.flexDirection = FlexStyle.FlexDirection.ROW
        mainPanel.style.setBorderRadius(0f)
        mainPanel.style.focusedStrokeColor = null
        mainPanel.style.border.isEnabled = false
        mainPanel.style.position = Style.PositionType.RELATIVE
        mainPanel.style.flexStyle.flexGrow = 1

        mainPanel.add(listPanel)
        mainPanel.add(context.framebuffer)

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
        mainPanel.add(rightPanel)

        container.add(menuPanel)
        container.add(mainPanel)
        container.add(animationPanel)
    }
}