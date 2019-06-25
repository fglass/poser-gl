package gui

import Processor
import gui.panel.AnimationPanel
import gui.panel.EditorPanel
import gui.panel.InformationPanel
import gui.panel.ListPanel
import org.joml.Vector2f
import org.liquidengine.legui.component.Panel
import org.liquidengine.legui.component.SelectBox
import render.PolygonMode
import shader.ShadingType

class Gui(position: Vector2f, size: Vector2f, private val context: Processor): Panel(position, size) {

    private val listPanel = ListPanel(this, context)
    val infoPanel = InformationPanel(this, context)
    val animationPanel = AnimationPanel(this, context)
    val editorPanel = EditorPanel(this, context)
    private val renderBox = SelectBox<String>(size.x - 175, 5f, 82f, 15f)
    private val shadingBox = SelectBox<String>(size.x - 87, 5f, 82f, 15f)

    fun createElements() {
        addToggles()
        add(listPanel)
        add(infoPanel)
        add(editorPanel)
        add(animationPanel)
        style.focusedStrokeColor = null
    }

    private fun addToggles() {
        val modes = arrayOf("Fill", "Vertices", "Wireframe")
        modes.forEach { renderBox.addElement(it) }

        renderBox.addSelectBoxChangeSelectionEventListener { event ->
            when (event.newValue.toString()) {
                modes[0] -> context.framebuffer.polygonMode = PolygonMode.FILL
                modes[1] -> context.framebuffer.polygonMode = PolygonMode.POINT
                modes[2] -> context.framebuffer.polygonMode = PolygonMode.LINE
            }
        }

        val types = arrayOf("Smooth", "Flat", "None")
        types.forEach { shadingBox.addElement(it) }

        shadingBox.addSelectBoxChangeSelectionEventListener { event ->
            when (event.newValue.toString()) {
                types[0] -> {
                    context.framebuffer.shadingType = ShadingType.SMOOTH
                    context.entity!!.reload(context.entityLoader)
                }
                types[1] -> {
                    context.framebuffer.shadingType = ShadingType.FLAT
                    context.entity!!.reload(context.entityLoader)
                }
                types[2] -> context.framebuffer.shadingType = ShadingType.NONE
            }
        }
        add(renderBox)
        add(shadingBox)
    }

    fun resize(size: Vector2f) {
        setSize(size)
        listPanel.resize()
        infoPanel.resize()
        editorPanel.resize()
        animationPanel.resize()
        renderBox.position = Vector2f(size.x - 175, 5f)
        shadingBox.position = Vector2f(size.x - 87, 5f)
    }
}