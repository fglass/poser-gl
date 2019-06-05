package gui

import Processor
import org.joml.Vector2f
import org.liquidengine.legui.component.CheckBox
import org.liquidengine.legui.component.Panel
import org.liquidengine.legui.component.SelectBox
import org.liquidengine.legui.component.TextInput
import org.liquidengine.legui.event.KeyEvent
import org.liquidengine.legui.event.MouseClickEvent
import org.lwjgl.glfw.GLFW
import shader.ShadingType

class Gui(position: Vector2f, size: Vector2f, private val context: Processor): Panel(position, size) {

    val npcItems = mutableListOf<NpcItem>()
    val animationItems = mutableListOf<AnimationItem>()
    private val npcManager = context.npcLoader.manager
    private val npcList = NpcList(5f, 49f, this, context, npcManager)
    private val animationList = AnimationList(5f, 49f, this, context)
    val infoPanel = InformationPanel(170f, 85f, this)

    fun createElements() {
        addToggles()
        addAnimationPane()
        addSearch()
        addSelectBox()
        add(npcList)
        add(infoPanel)
    }

    private fun addToggles() {
        val verticesToggle = CheckBox("Vertices", 160f, 0f, 60f, 24f)
        val wireframeToggle = CheckBox("Wireframe", 220f, 0f, 75f, 24f)
        val shadingBox = SelectBox<String>(297f, 5f, 75f, 15f)
        shadingBox.addElement("Smooth")
        shadingBox.addElement("Flat")
        shadingBox.addElement("None")

        verticesToggle.listenerMap.addListener(MouseClickEvent::class.java) {
            context.vertices = !context.vertices
            wireframeToggle.isChecked = false
            context.wireframe = false
        }
        wireframeToggle.listenerMap.addListener(MouseClickEvent::class.java) {
            context.wireframe = !context.wireframe
            verticesToggle.isChecked = false
            context.vertices = false
        }
        shadingBox.addSelectBoxChangeSelectionEventListener { event ->
            when (event.newValue.toString()) {
                "Smooth" -> {
                    context.shading = ShadingType.SMOOTH
                    context.npcLoader.reload()
                }
                "Flat" -> {
                    context.shading = ShadingType.FLAT
                    context.npcLoader.reload()
                }
                "None" -> context.shading = ShadingType.NONE
            }
        }
        add(verticesToggle)
        add(wireframeToggle)
        add(shadingBox)
    }

    private fun addAnimationPane() {
        /*val play = Button("Play", 710f, 470f, 40f, 24f)
        play.listenerMap.addListener(MouseClickEvent::class.java) { event ->
            if (MouseClickEvent.MouseClickAction.CLICK == event.action) {
                val animation = 1528
                context.animationHandler.playAnimation(animation)
            }
        }
        add(play)*/
    }

    private fun addSearch() {
        val searchField = TextInput("Search", 5f, 5f, 150f, 15f)
        searchField.listenerMap.addListener(MouseClickEvent::class.java) {
            if (searchField.textState.text == "Search") { // Placeholder text
                searchField.textState.text = ""
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
                        remove(animationList)
                        npcList.resetSearch()
                        add(npcList)
                    }
                }
                "Animations" -> {
                    if (contains(npcList)) {
                        remove(npcList)
                        animationList.resetSearch()
                        add(animationList)
                    }
                }
            }
        }
        add(selectBox)
    }

    fun resize(size: Vector2f) {
        setSize(size)
        context.glRenderer.reloadProjectionMatrix()
        infoPanel.resize()
        npcList.resize()
        animationList.resize()
    }
}