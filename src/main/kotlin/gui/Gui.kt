package gui

import Processor
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.joml.Vector2f
import org.liquidengine.legui.component.*
import org.liquidengine.legui.component.optional.align.HorizontalAlign
import org.liquidengine.legui.event.KeyEvent
import org.liquidengine.legui.event.MouseClickEvent
import org.liquidengine.legui.style.Style
import org.lwjgl.glfw.GLFW
import shader.ShadingType

class Gui(x: Float, y: Float, width: Float, height: Float, private val context: Processor): Panel(x, y, width, height) {

    private var loaded = 0
    private var list = ScrollablePanel(5f, 49f, getListSize().x, getListSize().y)
    private val listY = 2f
    private val listYOffset = 17
    private val npcItems = mutableListOf<NpcItem>()
    private val npcManager = context.npcLoader.manager

    private lateinit var infoWidget: Widget
    private lateinit var npcName: Label
    private lateinit var npcId: Label
    private lateinit var animationId: Label
    private lateinit var npcModels: Label

    fun createElements() {
        addToggles()
        addAnimationPane()
        addInformationBox()
        addSearch()
        addSelectBox()
        addList()
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
                animationId.textState.text = animation.toString()
            }
        }
        add(play)
    }

    private fun addInformationBox() {
        infoWidget = Widget("Information", getWidgetPosition(), Vector2f(170f, 100f))
        infoWidget.isResizable = false
        infoWidget.isDraggable = false
        infoWidget.isCloseable = false

        val name = Label("Name:", 5f, 5f, 80f, 15f)
        npcName = Label("N/A", 60f, 5f, 104f, 15f)
        npcName.textState.horizontalAlign = HorizontalAlign.RIGHT

        val id = Label("Id:", 5f, 20f, 100f, 15f)
        npcId = Label("N/A", 60f, 20f, 104f, 15f)
        npcId.textState.horizontalAlign = HorizontalAlign.RIGHT

        val animation = Label("Animation:", 5f, 35f, 100f, 15f)
        animationId = Label("N/A", 60f, 35f, 104f, 15f)
        animationId.textState.horizontalAlign = HorizontalAlign.RIGHT

        val models = Label("Model composition:", 5f, 50f, 100f, 15f)
        npcModels = Label("N/A", 5f, 65f, 159f, 15f)
        npcModels.textState.horizontalAlign = HorizontalAlign.RIGHT

        infoWidget.container.add(name)
        infoWidget.container.add(npcName)
        infoWidget.container.add(id)
        infoWidget.container.add(npcId)
        infoWidget.container.add(animation)
        infoWidget.container.add(animationId)
        infoWidget.container.add(models)
        infoWidget.container.add(npcModels)
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
                val filtered = (0..loaded).toList().filter {
                    val npc = npcManager.get(it)
                    npc.name != "null" && npc.name.toLowerCase().contains(search.textState.text)
                }

                list.verticalScrollBar.curValue = 0f // Reset scroll position
                list.container.setSize(142f, listY + filtered.size * listYOffset) // Adjust scroll size

                for (i in 0 until npcItems.size) {
                    val npcItem = npcItems[i]
                    when {
                        filtered.size >= loaded -> { // Reset as no matches
                            val npc = npcManager.get(i)
                            npcItem.npc = npc
                            npcItem.updateText()
                        }
                        i < filtered.size -> { // Shift matches up
                            val npc = npcManager.get(filtered[i])
                            npcItem.npc = npc
                            npcItem.updateText()
                        }
                        else -> npcItem.style.display = Style.DisplayType.NONE // Hide filtered
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

    private fun addList() {
        list.remove(list.horizontalScrollBar)
        val x = 2f
        var npcIndex = 0

        // Asynchronously add buttons
        GlobalScope.launch {
            for (i in 0 until npcManager.npcs.size) {
                val npc = npcManager.get(i)
                if (npc == null || npc.name == "null") {
                    continue
                }

                val npcItem = NpcItem(npc, x, listY + npcIndex++ * listYOffset, 137f, 14f)
                npcItems.add(npcItem)
                npcItem.listenerMap.addListener(MouseClickEvent::class.java) { event ->
                    if (MouseClickEvent.MouseClickAction.CLICK == event.action) {
                        context.selectNpc(npcItem.npc)
                    }
                }

                list.container.add(npcItem)
                list.container.setSize(142f, listY + npcIndex * listYOffset)
                remove(list)
                add(list)
                loaded = i
            }
            println("Loaded $npcIndex npcs")
        }
    }

    fun resize(size: Vector2f) {
        setSize(size)
        infoWidget.position = getWidgetPosition()
        list.size = getListSize()
    }

    private fun getListSize(): Vector2f {
        return Vector2f(150f, size.y - 52)
    }

    private fun getWidgetPosition(): Vector2f {
        return Vector2f(size.x - 180, 5f)
    }

    fun updateWidget() {
        if (!contains(infoWidget)) { // Hidden at start
             add(infoWidget)
        }
        val npc = context.npcLoader.currentNpc
        npcName.textState.text = npc.name
        npcId.textState.text = npc.id.toString()
        animationId.textState.text = "N/A"
        npcModels.textState.text = npc.models?.contentToString()
    }
}