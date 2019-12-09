package gui.component

import org.liquidengine.legui.component.*
import org.liquidengine.legui.component.optional.align.HorizontalAlign
import org.liquidengine.legui.style.Style
import org.liquidengine.legui.style.color.ColorConstants
import org.liquidengine.legui.style.flex.FlexStyle
import render.RenderContext
import util.setSizeLimits

class SettingsDialog(private val context: RenderContext): Dialog("Settings", "", context, 260f, 121f) {

    init {
        isDraggable = false
        container.style.display = Style.DisplayType.FLEX
        container.style.flexStyle.flexDirection = FlexStyle.FlexDirection.COLUMN
        addComponents()
    }

    private fun addComponents() {
        addSensitivitySlider()
        addBackgroundPicker()
        addGridToggle()
        addAdvancedToggle()
    }

    private fun addSensitivitySlider() {
        val row = getRow()
        val label = Label("Sensitivity:", 5f, 5f, 20f, 15f)
        val slider = Slider(135f, 7f, 100f, 10f, 50f)
        slider.sliderActiveColor = ColorConstants.white()
        row.add(label)
        row.add(slider)
        container.add(row)
    }

    private fun addBackgroundPicker() {
        val row = getRow()
        val label = Label("Background:", 5f, 5f, 20f, 15f)
        val input = TextInput("#ffffff", 138f, 5f, 50f, 15f)
        input.textState.horizontalAlign = HorizontalAlign.CENTER
        input.style.focusedStrokeColor = null
        row.add(label)
        row.add(input)
        container.add(row)
    }

    private fun addGridToggle() {
        val row = getRow()
        val label = Label("Grid:", 5f, 5f, 20f, 15f)
        val checkbox = CheckBox("", 135f, 5f, 20f, 15f)
        checkbox.isChecked = true
        checkbox.style.focusedStrokeColor = null
        row.add(label)
        row.add(checkbox)
        container.add(row)
    }

    private fun addAdvancedToggle() {
        val row = getRow()
        val label = Label("Advanced:", 5f, 5f, 20f, 15f)
        val checkbox = CheckBox("", 135f, 5f, 20f, 15f)
        checkbox.style.focusedStrokeColor = null
        row.add(label)
        row.add(checkbox)
        container.add(row)
    }

    private fun getRow(): Panel {
        val row = Panel()
        row.setSizeLimits(size.x, 25f)
        row.style.position = Style.PositionType.RELATIVE
        row.style.border.isEnabled = false
        row.style.focusedStrokeColor = null
        return row
    }
}