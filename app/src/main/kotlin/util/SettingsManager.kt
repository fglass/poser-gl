package util

import gui.component.SettingsDialog
import org.joml.Vector4f
import org.liquidengine.legui.style.color.ColorUtil
import render.RenderContext

class SettingsManager(private val context: RenderContext) {

    private lateinit var dialog: SettingsDialog
    var background: Vector4f = ColorUtil.fromInt(33, 33, 33, 1f)
    var gridActive = true
    var advancedMode = false

    fun openDialog() {
        dialog = SettingsDialog(context)
        dialog.display()
    }

    fun save() {

    }

    fun load() {

    }
}