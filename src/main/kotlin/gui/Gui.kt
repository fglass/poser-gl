package gui

import Processor
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import model.AnimationLoader
import org.joml.Vector2f
import org.liquidengine.legui.component.*
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
    private val buttons = mutableListOf<Button>()
    private val npcManager = context.npcLoader.manager

    fun createElements() {
        addToggles()
        addAnimationPane()
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
                    context.selectNpc(context.currentNpc) // Reload npc
                }
                "Flat" -> {
                    context.shading = ShadingType.FLAT
                    context.selectNpc(context.currentNpc)
                }
                "None" -> context.shading = ShadingType.NONE
            }
        }

        add(verticesToggle)
        add(wireframeToggle)
        add(shadingBox)
    }

    private fun addAnimationPane() {
        val play = Button("Play", 710f, 5f, 40f, 24f)
        play.listenerMap.addListener(MouseClickEvent::class.java) { event ->
            if (MouseClickEvent.MouseClickAction.CLICK == event.action) {
                AnimationLoader().loadAnimation(1528, context.entities[0].rawModel.definition, context.loader)
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
                val filtered = (0..loaded).toList().filter {
                    val npc = npcManager.get(it)
                    npc.name.toLowerCase().contains(search.textState.text)
                }

                list.verticalScrollBar.curValue = 0f // Reset scroll position
                list.container.setSize(142f, listY + filtered.size * listYOffset) // Adjust scroll size

                for (i in 0 until loaded) {
                    val button = buttons[i]
                    when {
                        filtered.size >= loaded -> { // Reset as no matches
                            button.style.display = Style.DisplayType.FLEX
                            button.textState.text = npcManager.get(i).name
                        }
                        i < filtered.size -> { // Shift matches up
                            button.style.display = Style.DisplayType.FLEX
                            button.textState.text =  npcManager.get(filtered[i]).name
                        }
                        else -> button.style.display = Style.DisplayType.NONE // Hide filtered
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
        val max = npcManager.npcs.size
        println("Loading npcs...")

        val x = 2f
        list.container.setSize(142f, listY + max * listYOffset)
        list.remove(list.horizontalScrollBar)

        // Asynchronously add labels
        GlobalScope.launch {
            for (i in 0 until max) {
                val npc = npcManager.get(i)
                val button = Button(npc.name, x, listY + i * listYOffset, 137f, 14f)

                buttons.add(button)
                button.listenerMap.addListener(MouseClickEvent::class.java) { event ->
                    if (MouseClickEvent.MouseClickAction.CLICK == event.action) {
                        context.selectNpc(button.textState.text)
                    }
                }

                list.container.add(button)
                remove(list)
                add(list)
                loaded = i
            }
            println("Loaded $max npcs")
        }
    }

    private fun getListSize(): Vector2f {
        return Vector2f(150f, size.y - 52)
    }

    fun resize(size: Vector2f) {
        setSize(size)
        list.size = getListSize()
    }
}