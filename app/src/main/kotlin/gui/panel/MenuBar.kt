package gui.panel

import gui.component.ImageButton
import org.joml.Vector2f
import org.liquidengine.legui.component.Panel
import org.liquidengine.legui.event.MouseClickEvent
import org.liquidengine.legui.style.Style
import org.liquidengine.legui.style.color.ColorConstants
import org.liquidengine.legui.style.flex.FlexStyle
import render.RenderContext
import util.ResourceMap
import util.setHeightLimit
import util.setSizeLimits

class MenuBar(context: RenderContext): Panel() { // TODO: home button
    
    init {
        style.display = Style.DisplayType.FLEX
        style.flexStyle.flexDirection = FlexStyle.FlexDirection.ROW
        style.position = Style.PositionType.RELATIVE
        style.flexStyle.flexGrow = 1
        style.focusedStrokeColor = null
        style.background.color = ColorConstants.darkGray()
        style.setMargin(5f, 5f, 1f, 5f)
        setHeightLimit(24f)

        addMenuButton("pack", context.cacheService.packManager::pack, marginLeft = 2f)
        addMenuButton("export", context.exportManager::openDialog)
        addMenuButton("import", context.importManager::import)
        addMenuButton("undo", context.animationHandler.history::undo)
        addMenuButton("redo", context.animationHandler.history::redo)
        addMenuButton("settings", context.settingsManager::openDialog)
    }

    private fun addMenuButton(name: String, action: () -> Unit, marginLeft: Float = 1f) {
        val button = ImageButton(Vector2f(), ResourceMap[name], name.capitalize())
        button.hoveredIcon = ResourceMap["$name-hovered"]

        button.listenerMap.addListener(MouseClickEvent::class.java) { event ->
            if (event.action == MouseClickEvent.MouseClickAction.CLICK) {
                action.invoke()
            }
        }

        button.setSizeLimits(23f, 23f)
        button.style.setMarginTop(1f)
        button.style.setMarginLeft(marginLeft)
        button.style.position = Style.PositionType.RELATIVE
        add(button)
    }
}