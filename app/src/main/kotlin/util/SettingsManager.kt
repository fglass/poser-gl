package util

import gui.BACKGROUND
import gui.component.SettingsDialog
import org.joml.Vector4f
import render.RenderContext
import java.io.FileOutputStream
import java.util.*

class SettingsManager(private val context: RenderContext) {

    private lateinit var dialog: SettingsDialog
    var background: Vector4f = BACKGROUND // TODO: remove BACKGROUND and use BackgroundColour
    var sensitivityMultiplier = 1f
    var gridActive = true
    var advancedMode = false

    fun openDialog() {
        dialog = SettingsDialog(context)
        dialog.display()
    }

    fun save() {
        val properties = Properties()

        properties["background"] = background.toString()
        properties["sensitivity"] = sensitivityMultiplier.toString()
        properties["grid"] = gridActive.toString()
        properties["advanced"] = advancedMode.toString()

        val propertiesFile = ".settings"
        val fileOutputStream = FileOutputStream(propertiesFile)
        properties.store(fileOutputStream, null)
    }

    fun load() {

    }
}