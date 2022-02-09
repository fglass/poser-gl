package gui.component

import com.spinyowl.legui.component.*
import com.spinyowl.legui.component.event.checkbox.CheckBoxChangeValueEvent
import com.spinyowl.legui.component.event.slider.SliderChangeValueEvent
import com.spinyowl.legui.listener.EventListener
import com.spinyowl.legui.style.Style
import com.spinyowl.legui.style.color.ColorConstants
import com.spinyowl.legui.style.flex.FlexStyle
import render.RenderContext
import util.Colour
import util.setSizeLimits
import kotlin.math.max

private const val SLIDER_OFFSET = 50f

class SettingsDialog(private val context: RenderContext): Dialog("Settings", "", context, 260f, 209f) {

    init {
        isDraggable = false
        container.style.display = Style.DisplayType.FLEX
        container.style.flexStyle.flexDirection = FlexStyle.FlexDirection.COLUMN
        addComponents()
        addWidgetCloseEventListener { context.settingsManager.save() }
    }

    private fun addComponents() {
        addBackgroundPicker()
        addCursorPicker()
        addZoomSensitivitySlider()
        addCameraSensitivitySlider()
        addGridToggle()
        addBonesToggle()
        addAdvancedToggle()
    }

    private fun addBackgroundPicker() {
        addColourPicker(
            field = "Background", colours = Colour.values(), default = context.settingsManager.background,
            onSelect = { context.settingsManager.background = it }
        )
    }

    private fun addCursorPicker() {
        addColourPicker(
            field = "Cursor", colours = arrayOf(Colour.RED, Colour.GREEN, Colour.BLUE),
            default = context.settingsManager.cursorColour, onSelect = {
                context.settingsManager.cursorColour = it
                context.gui.animationPanel.setCursorColour(it)
            }
        )
    }

    private fun addColourPicker(field: String, colours: Array<Colour>, default: Colour, onSelect: (Colour) -> Unit) {
        val row = getRow()
        row.style.setMarginTop(5f)

        val label = Label("$field:", 5f, 5f, 20f, 15f)
        val dropdown = SelectBox<Colour>(138f, 5f, 95f, 15f)
        dropdown.expandButton.style.border.isEnabled = false
        dropdown.childComponents.forEach { it.style.focusedStrokeColor = null }

        colours.forEach { dropdown.addElement(it) }
        dropdown.setSelected(default, true)
        dropdown.addSelectBoxChangeSelectionEventListener { onSelect(it.newValue) }

        row.add(label)
        row.add(dropdown)
        container.add(row)
    }

    private fun addCameraSensitivitySlider() {
        addSensitivitySlider("Camera", context.settingsManager.cameraSensitivityMultiplier) {
            context.settingsManager.cameraSensitivityMultiplier = max(it.newValue / SLIDER_OFFSET, 0.01f)
        }
    }

    private fun addZoomSensitivitySlider() {
        addSensitivitySlider("Zoom", context.settingsManager.zoomSensitivityMultiplier) {
            context.settingsManager.zoomSensitivityMultiplier = max(it.newValue / SLIDER_OFFSET, 0.01f)
        }
    }

    private fun addSensitivitySlider(type: String, value: Float, onChange: EventListener<SliderChangeValueEvent<Slider>>) {
        val label = Label("$type Sensitivity:", 5f, 5f, 20f, 15f)

        val slider = Slider(135f, 7f, 100f, 10f, value * SLIDER_OFFSET)
        slider.sliderActiveColor = ColorConstants.white()
        slider.addSliderChangeValueEventListener(onChange)

        val row = getRow()
        row.add(label)
        row.add(slider)
        container.add(row)
    }

    private fun addGridToggle() {
        val listener = EventListener<CheckBoxChangeValueEvent<CheckBox>> {
            context.settingsManager.gridActive = it.targetComponent.isChecked
        }
        addToggle("Grid", context.settingsManager.gridActive, listener)
    }

    private fun addBonesToggle() {
        val listener = EventListener<CheckBoxChangeValueEvent<CheckBox>> {
            context.settingsManager.bonesActive = it.targetComponent.isChecked
        }
        addToggle("Bones", context.settingsManager.bonesActive, listener)
    }

    private fun addAdvancedToggle() {
        val listener = EventListener<CheckBoxChangeValueEvent<CheckBox>> {
            context.settingsManager.advancedMode = it.targetComponent.isChecked
            context.animationHandler.currentAnimation?.reload()
        }
        addToggle("Advanced", context.settingsManager.advancedMode, listener)
    }

    private fun addToggle(name: String, default: Boolean, onToggle: EventListener<CheckBoxChangeValueEvent<CheckBox>>) {
        val label = Label("$name:", 5f, 5f, 20f, 15f)

        val checkbox = CheckBox("", 178f, 5f, 20f, 15f)
        checkbox.style.focusedStrokeColor = null
        checkbox.isChecked = default
        checkbox.addCheckBoxChangeValueListener(onToggle)

        val row = getRow()
        row.add(label)
        row.add(checkbox)
        container.add(row)
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