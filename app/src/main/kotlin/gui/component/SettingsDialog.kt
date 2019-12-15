package gui.component

import org.liquidengine.legui.component.*
import org.liquidengine.legui.component.event.checkbox.CheckBoxChangeValueEvent
import org.liquidengine.legui.listener.EventListener
import org.liquidengine.legui.style.Style
import org.liquidengine.legui.style.color.ColorConstants
import org.liquidengine.legui.style.flex.FlexStyle
import render.RenderContext
import util.Colour
import util.setSizeLimits
import kotlin.math.max

class SettingsDialog(private val context: RenderContext): Dialog("Settings", "", context, 260f, 154f) {

    init {
        isDraggable = false
        container.style.display = Style.DisplayType.FLEX
        container.style.flexStyle.flexDirection = FlexStyle.FlexDirection.COLUMN
        addComponents()
        addWidgetCloseEventListener { context.settingsManager.save() }
    }

    private fun addComponents() {
        addBackgroundPicker()
        addSensitivitySlider()
        addGridToggle()
        addJointsToggle()
        addAdvancedToggle()
    }

    private fun addBackgroundPicker() {
        val row = getRow()
        row.style.setMarginTop(5f)

        val label = Label("Background:", 5f, 5f, 20f, 15f)
        val colours = SelectBox<Colour>(138f, 5f, 95f, 15f)
        colours.expandButton.style.border.isEnabled = false
        colours.childComponents.forEach { it.style.focusedStrokeColor = null }

        Colour.values().forEach { colours.addElement(it) }
        colours.setSelected(context.settingsManager.background, true)
        colours.addSelectBoxChangeSelectionEventListener {
            context.settingsManager.background = it.newValue
        }

        row.add(label)
        row.add(colours)
        container.add(row)
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

    private fun addGridToggle() {
        val listener = EventListener<CheckBoxChangeValueEvent<CheckBox>> {
            context.settingsManager.gridActive = it.targetComponent.isChecked
        }
        addToggle("Grid", context.settingsManager.gridActive, listener)
    }

    private fun addJointsToggle() {
        val listener = EventListener<CheckBoxChangeValueEvent<CheckBox>> {
            context.settingsManager.jointsActive = it.targetComponent.isChecked
        }
        addToggle("Joints", context.settingsManager.jointsActive, listener)
    }

    private fun addAdvancedToggle() {
        val listener = EventListener<CheckBoxChangeValueEvent<CheckBox>> {
            context.settingsManager.advancedMode = it.targetComponent.isChecked
            context.animationHandler.currentAnimation?.reload()
        }
        addToggle("Advanced", context.settingsManager.advancedMode, listener)
    }

    private fun addToggle(name: String, default: Boolean, onToggle: EventListener<CheckBoxChangeValueEvent<CheckBox>>) {
        val row = getRow()
        val label = Label("$name:", 5f, 5f, 20f, 15f)

        val checkbox = CheckBox("", 178f, 5f, 20f, 15f)
        checkbox.style.focusedStrokeColor = null
        checkbox.isChecked = default
        checkbox.addCheckBoxChangeValueListener(onToggle)

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