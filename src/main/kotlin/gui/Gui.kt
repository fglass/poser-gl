package gui

import Processor
import org.joml.Vector2f
import org.liquidengine.legui.component.*
import org.liquidengine.legui.event.KeyEvent
import org.liquidengine.legui.event.MouseClickEvent
import org.lwjgl.glfw.GLFW
import shader.ShadingType

class Gui(x: Float, y: Float, width: Float, height: Float, private val context: Processor): Panel(x, y, width, height) {

    val npcItems = mutableListOf<NpcItem>()
    private val npcManager = context.npcLoader.manager
    private var npcList = NpcList(5f, 49f, this, context, npcManager)

    val animationItems = mutableListOf<AnimationItem>()
    private var animationList = AnimationList(5f, 49f, this, context)
    var infoWidget = InformationWidget(170f, 100f, this)

    fun createElements() {
        addToggles()
        addAnimationPane()
        addSearch()
        addSelectBox()
        add(npcList)
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
                    context.reloadNpc()
                }
                "Flat" -> {
                    context.shading = ShadingType.FLAT
                    context.reloadNpc()
                }
                "None" -> context.shading = ShadingType.NONE
            }
        }
        add(verticesToggle)
        add(wireframeToggle)
        add(shadingBox)
    }

    private fun addAnimationPane() { // TODO
        val play = Button("Play", 710f, 470f, 40f, 24f)
        play.listenerMap.addListener(MouseClickEvent::class.java) { event ->
            if (MouseClickEvent.MouseClickAction.CLICK == event.action) {
                //val animation = 1528
                //context.animationHandler.playAnimation(animation)
            }
        }
        add(play)
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
                if (contains(npcList)) npcList.search(searchField) else animationList.search(searchField)

            }
        }
        add(searchField)
    }

    private fun addSelectBox() {
        val selectBox = SelectBox<String>(5f, 27f, 150f, 15f)
        selectBox.addElement("NPCs")
        selectBox.addElement("Animations")

        selectBox.addSelectBoxChangeSelectionEventListener {
            if (contains(npcList)) {
                remove(npcList)
                add(animationList)
            } else {
                remove(animationList)
                add(npcList)
            }
        }

        add(selectBox)
    }

    fun resize(size: Vector2f) {
        setSize(size)
        infoWidget.position = infoWidget.getWidgetPosition()
        npcList.size = npcList.getListSize()
    }

    fun updateWidget() {
        if (!contains(infoWidget)) { // Hidden at start
             add(infoWidget)
        }
        infoWidget.update(context.npcLoader.currentNpc)
    }
}