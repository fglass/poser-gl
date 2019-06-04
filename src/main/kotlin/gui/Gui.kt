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
    private var infoWidget = InformationWidget(170f, 100f, this)

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

    private fun addAnimationPane() {
        val play = Button("Play", 710f, 470f, 40f, 24f)
        play.listenerMap.addListener(MouseClickEvent::class.java) { event ->
            if (MouseClickEvent.MouseClickAction.CLICK == event.action) {
                val animation = 1528
                context.animationHandler.loadAnimation(animation)
                infoWidget.animationId.textState.text = animation.toString()
            }
        }
        add(play)
    }

    private fun addSearch() {
        val search = TextInput("Search", 5f, 5f, 150f, 15f)
        search.listenerMap.addListener(MouseClickEvent::class.java) {
            if (search.textState.text == "Search") {
                search.textState.text = ""
            }
        }
        search.listenerMap.addListener(KeyEvent::class.java) { event ->
            if (event.action == GLFW.GLFW_RELEASE) {
                val filtered = (0..npcList.maxIndex).toList().filter {
                    val npc = npcManager.get(it)
                    npc.name != "null" && npc.name.toLowerCase().contains(search.textState.text)
                }
                npcList.adjustScroll(filtered.size)

                for (i in 0 until npcItems.size) {
                    val npcItem = npcItems[i]
                    when {
                        filtered.size >= npcList.maxIndex -> { // Reset as no matches
                            val npc = npcManager.get(i)
                            npcItem.npc = npc
                            npcItem.updateText()
                        }
                        i < filtered.size -> { // Shift matches up
                            val npc = npcManager.get(filtered[i])
                            npcItem.npc = npc
                            npcItem.updateText()
                        }
                        else -> npcItem.hide() // Hide filtered
                    }
                }
            }
        }
        add(search)
    }

    private fun addSelectBox() {
        val selectBox = SelectBox<String>(5f, 27f, 150f, 15f)
        selectBox.addElement("NPCs")
        selectBox.addElement("Animations")
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