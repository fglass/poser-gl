package gui

import Processor
import gui.panel.AnimationPanel
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
    private val renderBox = SelectBox<String>(size.x - 175, 27f, 82f, 15f)
    private val shadingBox = SelectBox<String>(size.x - 87, 27f, 82f, 15f)

    fun createElements() {
        addToggles()
        add(listPanel)
        add(infoPanel)
        add(animationPanel)
        style.focusedStrokeColor = null
    }

    private fun addToggles() {
        renderBox.addElement("Fill")
        renderBox.addElement("Vertices")
        renderBox.addElement("Wireframe")

        renderBox.addSelectBoxChangeSelectionEventListener { event ->
            when (event.newValue.toString()) {
                "Fill" -> context.framebuffer.polygonMode = PolygonMode.FILL
                "Vertices" -> context.framebuffer.polygonMode = PolygonMode.POINT
                "Wireframe" -> context.framebuffer.polygonMode = PolygonMode.LINE
            }
        }

        shadingBox.addElement("Smooth")
        shadingBox.addElement("Flat")
        shadingBox.addElement("None")

        shadingBox.addSelectBoxChangeSelectionEventListener { event ->
            when (event.newValue.toString()) {
                "Smooth" -> {
                    context.framebuffer.shadingType = ShadingType.SMOOTH
                    context.entity!!.reload(context.npcLoader)
                }
                "Flat" -> {
                    context.framebuffer.shadingType = ShadingType.FLAT
                    context.entity!!.reload(context.npcLoader)
                }
                "None" -> context.framebuffer.shadingType = ShadingType.NONE
            }
        }
        add(renderBox)
        add(shadingBox)
    }

    fun resize(size: Vector2f) {
        setSize(size)
        listPanel.resize()
        infoPanel.resize()
        animationPanel.resize()
        renderBox.position = Vector2f(size.x - 175, 27f)
        shadingBox.position = Vector2f(size.x - 87, 27f)
    }
}