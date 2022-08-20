package gui.component

import api.API_VERSION
import org.liquidengine.legui.component.Label
import org.liquidengine.legui.component.Panel
import org.liquidengine.legui.style.Style
import org.liquidengine.legui.style.flex.FlexStyle
import render.APP_VERSION
import render.RenderContext
import util.setSizeLimits

class AboutDialog(context: RenderContext): Dialog("About", "", context, 260f, 99f) {

    init {
        isDraggable = false
        container.style.display = Style.DisplayType.FLEX
        container.style.flexStyle.flexDirection = FlexStyle.FlexDirection.COLUMN
        addComponents()
    }

    private fun addComponents() {
        val labels = arrayOf("App Version" to APP_VERSION, "API Version" to API_VERSION, "Contact" to "fred#0006")
        labels.forEach {
            val row = getRow()
            row.add(Label("${it.first}:", 5f, 5f, 20f, 15f))
            row.add(Label(it.second, 178f, 5f, 20f, 15f))
            container.add(row)
        }
    }

    private fun getRow(): Panel {
        val row = Panel()
        row.setSizeLimits(size.x, 25f)
        row.style.setMarginLeft(5f)
        row.style.position = Style.PositionType.RELATIVE
        row.style.border.isEnabled = false
        row.style.focusedStrokeColor = null
        return row
    }
}