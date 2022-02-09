package gui

import com.spinyowl.legui.component.Layer
import com.spinyowl.legui.component.Panel
import com.spinyowl.legui.event.MouseClickEvent
import com.spinyowl.legui.style.Style
import com.spinyowl.legui.style.color.ColorConstants
import com.spinyowl.legui.style.flex.FlexStyle
import com.spinyowl.legui.style.length.LengthType
import gui.panel.*
import render.RenderContext

class GuiManager(context: RenderContext) {
    val container: Layer = context.frame.componentLayer
    val listPanel = ListPanel(context)
    val managerPanel = ManagerPanel(context)
    val editorPanel = EditorPanel(context)
    val animationPanel = AnimationPanel(context)
    private val menuBar = MenuBar(context)

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
        mainPanel.style.setMarginBottom(1f)

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

        mainPanel.style.position = Style.PositionType.RELATIVE;
        animationPanel.style.position = Style.PositionType.RELATIVE;

        container.add(menuBar)
        container.add(mainPanel)
        container.add(animationPanel)
    }
}