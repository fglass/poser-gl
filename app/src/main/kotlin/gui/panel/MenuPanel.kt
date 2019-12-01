package gui.panel

import gui.component.ImageButton
import org.joml.Vector2f
import org.liquidengine.legui.component.Panel
import org.liquidengine.legui.event.MouseClickEvent
import org.liquidengine.legui.image.BufferedImage
import org.liquidengine.legui.style.Style
import org.liquidengine.legui.style.border.SimpleLineBorder
import org.liquidengine.legui.style.color.ColorConstants
import org.liquidengine.legui.style.color.ColorUtil
import org.liquidengine.legui.style.flex.FlexStyle
import org.liquidengine.legui.style.length.Auto
import org.liquidengine.legui.style.length.LengthType
import render.RenderContext
import render.SPRITE_PATH
import util.setHeightLimit
import util.setSizeLimits

class MenuPanel(context: RenderContext): Panel() {
    
    init {
        style.display = Style.DisplayType.FLEX
        style.flexStyle.flexDirection = FlexStyle.FlexDirection.ROW
        //style.setBorderRadius(0f)
        style.focusedStrokeColor = null
        //style.border.isEnabled = false
        style.position = Style.PositionType.RELATIVE
        style.flexStyle.flexGrow = 1
        //style.border = SimpleLineBorder(ColorUtil.fromInt(35, 35, 35, 1f), 1f)
        //style.background.color = ColorConstants.blue()
        style.background.color = ColorConstants.darkGray()
        //style.setMarginBottom(1f)
        setHeightLimit(24f)
        style.setMargin(5f, 5f, 1f, 5f)

        addMenuButton("pack", context.cacheService::pack)
        addMenuButton("export", context.exportManager::openDialog)
        addMenuButton("import", context.importManager::import)
    }

    private fun addMenuButton(name: String, action: () -> Unit): ImageButton {
        val button = ImageButton(Vector2f(), BufferedImage("$SPRITE_PATH$name.png"), name.capitalize())
        button.hoveredIcon = BufferedImage("$SPRITE_PATH$name-hovered.png")

        button.listenerMap.addListener(MouseClickEvent::class.java) { event ->
            if (event.action == MouseClickEvent.MouseClickAction.CLICK) {
                action.invoke()
            }
        }

        button.setSizeLimits(23f, 23f)
        button.style.setMarginTop(1f)
        button.style.position = Style.PositionType.RELATIVE
        add(button)
        return button
    }

}