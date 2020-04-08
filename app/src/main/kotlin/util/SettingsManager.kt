package util

import gui.component.SettingsDialog
import mu.KotlinLogging
import render.RenderContext
import java.io.FileReader
import java.io.FileWriter
import java.util.*

private val logger = KotlinLogging.logger {}

const val SETTINGS_FILE = "app.settings"

class SettingsManager(private val context: RenderContext) {

    var background = Colour.GRAY
    var cursorColour = Colour.GREEN
    var sensitivityMultiplier = 1f
    var gridActive = true
    var jointsActive = true
    var advancedMode = false

    fun save() {
        val properties = Properties()

        properties["background"] = background.toString()
        properties["cursorColour"] = cursorColour.toString()
        properties["sensitivity"] = sensitivityMultiplier.toString()
        properties["grid"] = gridActive.toString()
        properties["joints"] = jointsActive.toString()
        properties["advanced"] = advancedMode.toString()

        val fileWriter = FileWriter(SETTINGS_FILE)
        properties.store(fileWriter, null)
    }

    fun load() {
        try {
            val properties = Properties()
            val reader = FileReader(SETTINGS_FILE)
            properties.load(reader)

            properties.getProperty("background")?.let {
                background = Colour.valueOf(it.toUpperCase())
            }
            properties.getProperty("cursorColour")?.let {
                cursorColour = Colour.valueOf(it.toUpperCase())
            }
            properties.getProperty("sensitivity")?.let {
                sensitivityMultiplier = it.toFloat()
            }
            properties.getProperty("grid")?.let {
                gridActive = it.toBoolean()
            }
            properties.getProperty("joints")?.let {
                jointsActive = it.toBoolean()
            }
            properties.getProperty("advanced")?.let {
                advancedMode = it.toBoolean()
            }
        } catch (e: Exception) {
            logger.warn(e) { "Failed to load user settings" }
        }
    }

    fun openDialog() {
        SettingsDialog(context).display()
    }
}