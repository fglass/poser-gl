package gui

import Processor
import gui.component.AnimationList
import gui.component.NpcList
import gui.panel.AnimationPanel
import gui.panel.InformationPanel
import org.joml.Vector2f
import org.liquidengine.legui.component.Panel
import org.liquidengine.legui.component.SelectBox
import org.liquidengine.legui.component.TextInput
import org.liquidengine.legui.event.KeyEvent
import org.liquidengine.legui.event.MouseClickEvent
import org.liquidengine.legui.style.color.ColorConstants
import org.lwjgl.glfw.GLFW
import render.PolygonMode
import shader.ShadingType

class Gui(position: Vector2f, size: Vector2f, private val context: Processor): Panel(position, size) {

    val npcItems = mutableListOf<NpcList.NpcItem>()
    val animationItems = mutableListOf<AnimationList.AnimationItem>()
    private val npcManager = context.npcLoader.manager
    private val npcList = NpcList(5f, 49f, this, context, npcManager)
    private val animationList = AnimationList(5f, 49f, this, context)

    val infoPanel = InformationPanel(this, context)
    val animationPanel = AnimationPanel(this, context)
    private val renderBox = SelectBox<String>(size.x - 175, 27f, 82f, 15f)
    private val shadingBox = SelectBox<String>(size.x - 87, 27f, 82f, 15f)

    fun createElements() {
        addToggles()
        addSearch()
        addSelectBox()
        add(npcList)
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
                    context.npcLoader.reload()
                }
                "Flat" -> {
                    context.framebuffer.shadingType = ShadingType.FLAT
                    context.npcLoader.reload()
                }
                "None" -> context.framebuffer.shadingType = ShadingType.NONE
            }
        }
        add(renderBox)
        add(shadingBox)
    }

    private fun addSearch() {
        val searchField = TextInput("Search", 5f, 5f, 150f, 15f)
        searchField.textState.textColor = ColorConstants.gray()

        searchField.listenerMap.addListener(MouseClickEvent::class.java) {
            if (searchField.textState.text == "Search") { // Placeholder text
                searchField.textState.text = ""
                searchField.textState.textColor = ColorConstants.white()
            }
        }
        searchField.listenerMap.addListener(KeyEvent::class.java) { event ->
            if (event.action == GLFW.GLFW_RELEASE) {
                val input = searchField.textState.text
                if (contains(npcList)) npcList.search(input) else animationList.search(input)
            }
        }
        add(searchField)
    }

    private fun addSelectBox() {
        val selectBox = SelectBox<String>(5f, 27f, 150f, 15f)
        selectBox.addElement("NPCs")
        selectBox.addElement("Animations")

        selectBox.addSelectBoxChangeSelectionEventListener { event ->
            when (event.newValue.toString()) {
                "NPCs" -> {
                    if (contains(animationList)) {
                        npcList.resetSearch()
                        remove(animationList)
                        add(npcList)
                    }
                }
                "Animations" -> {
                    if (contains(npcList)) {
                        animationList.resetSearch()
                        remove(npcList)
                        add(animationList)
                    }
                }
            }
        }
        add(selectBox)
    }

    fun resize(size: Vector2f) {
        setSize(size)
        npcList.resize()
        animationList.resize()
        infoPanel.resize()
        animationPanel.resize()
        renderBox.position = Vector2f(size.x - 175, 27f)
        shadingBox.position = Vector2f(size.x - 87, 27f)
    }
}