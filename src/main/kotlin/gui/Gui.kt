package gui

import Processor
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.joml.Vector2f
import org.liquidengine.legui.component.*
import org.liquidengine.legui.event.KeyEvent
import org.liquidengine.legui.event.MouseClickEvent
import org.liquidengine.legui.style.Style
import org.lwjgl.glfw.GLFW

class Gui(x: Float, y: Float, width: Float, height: Float, private val context: Processor): Panel(x, y, width, height) {

    private var loaded = 0
    private var max = 0
    private var list = ScrollablePanel(5f, 27f, 150f, size.y - 38)
    private val buttons = mutableListOf<Button>()

    fun resize(size: Vector2f) {
        setSize(size)
        list.setSize(150f, size.y - 38)
    }

    fun createElements() {
        addToggles()
        addSearch()
        addModelList()
    }

    private fun addToggles() {
        val shadingToggle = CheckBox("Shading", 160f, 0f, 60f, 24f)
        val verticesToggle = CheckBox("Vertices", 220f, 0f, 60f, 24f)
        val wireframeToggle = CheckBox("Wireframe", 280f, 0f, 80f, 24f)

        shadingToggle.isChecked = true
        shadingToggle.listenerMap.addListener(MouseClickEvent::class.java) {
            context.shading = !context.shading
        }
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

        add(shadingToggle)
        add(verticesToggle)
        add(wireframeToggle)
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
                list.verticalScrollBar.curValue = 0f // Reset scroll
                val range = (0..loaded).toList()
                val filtered = range.filter { it.toString().startsWith(search.textState.text) }

                for (i in 0 until loaded) {
                    val button = buttons[i]
                    when {
                        filtered.size >= loaded -> { // Reset as no matches
                            button.style.display = Style.DisplayType.FLEX
                            button.textState.text = i.toString()
                        }
                        i < filtered.size -> { // Shift matches up
                            button.style.display = Style.DisplayType.FLEX
                            button.textState.text = filtered[i].toString()
                        }
                        else -> button.style.display = Style.DisplayType.NONE // Hide filtered
                    }
                }
            }
        }
        add(search)
    }

    private fun addModelList() {
        max = context.getMaxModels()
        println("Loading models...")

        val x = 2f
        val y = 2f
        val yOffset = 13
        list.container.setSize(142f, y + max * yOffset)
        list.remove(list.horizontalScrollBar)

        // Asynchronously add labels
        GlobalScope.launch {
            for (i in 0 until max) {
                val button = Button(i.toString(), x, y + i * yOffset, 137f, 10f)

                buttons.add(button)
                button.listenerMap.addListener(MouseClickEvent::class.java) { event ->
                    if (MouseClickEvent.MouseClickAction.CLICK == event.action) {
                        context.setModel(button.textState.text.toInt()) // Use text state to aid with searching
                    }
                }

                list.container.add(button)
                remove(list)
                add(list)
                loaded = i
            }
            println("Loaded $max models")
        }
    }
}