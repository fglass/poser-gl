package gui

import HEIGHT
import WIDTH
import Processor
import gui.panel.AnimationPanel
import gui.panel.EditorPanel
import gui.panel.ListPanel
import gui.panel.ManagerPanel
import org.joml.Vector2f
import org.liquidengine.legui.component.Panel

class GuiManager(position: Vector2f, size: Vector2f, context: Processor): Panel(position, size) {

    val listPanel = ListPanel(this, context)
    val managerPanel = ManagerPanel(this, context)
    val animationPanel = AnimationPanel(this, context)
    val editorPanel = EditorPanel(this, context)

    fun createElements() {
        style.setBorderRadius(0f)
        add(listPanel)
        add(managerPanel)
        add(editorPanel)
        add(animationPanel)
        style.focusedStrokeColor = null
    }

    fun resize(size: Vector2f) {
        setSize(size)
        WIDTH = size.x.toInt()
        HEIGHT = size.y.toInt()

        listPanel.resize()
        managerPanel.resize()
        editorPanel.resize()
        animationPanel.resize()
    }
}