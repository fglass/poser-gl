package util

import gui.component.SettingsDialog
import render.RenderContext
import java.io.FileOutputStream
import java.util.*

class SettingsManager(private val context: RenderContext) {

    private lateinit var dialog: SettingsDialog
    var background = Colour.GRAY
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