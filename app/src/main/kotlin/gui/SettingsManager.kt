package gui

import gui.component.SettingsDialog
import render.RenderContext

class SettingsManager(private val context: RenderContext) {

    private lateinit var dialog: SettingsDialog

    fun openDialog() {
        dialog = SettingsDialog(context)
        dialog.display()
    }
}