package gui.component

import gui.BACKGROUND
import org.joml.Vector4f
import org.liquidengine.legui.component.*
import org.liquidengine.legui.component.optional.align.HorizontalAlign
import org.liquidengine.legui.style.Style
import org.liquidengine.legui.style.color.ColorConstants
import org.liquidengine.legui.style.flex.FlexStyle
import render.RenderContext
import util.setSizeLimits
import kotlin.math.max

class SettingsDialog(private val context: RenderContext): Dialog("Settings", "", context, 260f, 121f) {

    init {
        isDraggable = false
        container.style.display = Style.DisplayType.FLEX
        container.style.flexStyle.flexDirection = FlexStyle.FlexDirection.COLUMN
        addComponents()

        addWidgetCloseEventListener { context.settingsManager.save() }
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

        val offset = 50f
        val value = context.settingsManager.sensitivityMultiplier * offset
        val slider = Slider(135f, 7f, 100f, 10f, value)
        slider.sliderActiveColor = ColorConstants.white()

        slider.addSliderChangeValueEventListener {
            context.settingsManager.sensitivityMultiplier = max(it.newValue / offset, 0.01f)
        }

        row.add(label)
        row.add(slider)
        container.add(row)
    }

    private fun addBackgroundPicker() {
        val row = getRow()
        val label = Label("Background:", 5f, 5f, 20f, 15f)

        val colours = SelectBox<BackgroundColour>(138f, 5f, 95f, 15f)
        colours.expandButton.style.border.isEnabled = false
        colours.childComponents.forEach { it.style.focusedStrokeColor = null }

        BackgroundColour.values().forEach { colours.addElement(it) }
        colours.setSelected(BackgroundColour[context.settingsManager.background], true)
        colours.addSelectBoxChangeSelectionEventListener {
            context.settingsManager.background = it.newValue.colour
        }

        row.add(label)
        row.add(colours)
        container.add(row)
    }

    private fun addGridToggle() {
        val row = getRow()
        val label = Label("Grid:", 5f, 5f, 20f, 15f)

        val checkbox = CheckBox("", 135f, 5f, 20f, 15f)
        checkbox.style.focusedStrokeColor = null
        checkbox.isChecked = context.settingsManager.gridActive
        checkbox.addCheckBoxChangeValueListener {
            context.settingsManager.gridActive = it.targetComponent.isChecked
        }

        row.add(label)
        row.add(checkbox)
        container.add(row)
    }

    private fun addAdvancedToggle() {
        val row = getRow()
        val label = Label("Advanced:", 5f, 5f, 20f, 15f)

        val checkbox = CheckBox("", 135f, 5f, 20f, 15f)
        checkbox.style.focusedStrokeColor = null
        checkbox.isChecked = context.settingsManager.advancedMode
        checkbox.addCheckBoxChangeValueListener {
            context.settingsManager.advancedMode = it.targetComponent.isChecked
        }

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

enum class BackgroundColour(val colour: Vector4f) {
    GRAY(BACKGROUND),
    BLACK(ColorConstants.black()),
    WHITE(ColorConstants.white()),
    RED(ColorConstants.lightRed()),
    GREEN(ColorConstants.lightGreen()),
    BLUE(ColorConstants.lightBlue());

    override fun toString() = super.toString().toLowerCase().capitalize()

    companion object {
        private val map = values().associateBy(BackgroundColour::colour)
        operator fun get(color: Vector4f) = map[color] ?: error("Invalid color")
    }
}