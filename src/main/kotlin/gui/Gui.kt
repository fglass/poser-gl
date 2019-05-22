package gui

import Processor
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.liquidengine.legui.component.*
import org.liquidengine.legui.event.KeyEvent
import org.liquidengine.legui.event.MouseClickEvent
import org.liquidengine.legui.style.Style
import org.lwjgl.glfw.GLFW

class Gui(x: Float, y: Float, width: Float, height: Float, private val context: Processor): Panel(x, y, width, height) {

    private var loaded = false
    private var max = 0
    private val buttons = mutableListOf<Button>()

    fun createElements() {
        addToggles()
        addSearch()
        addModelList()
    }

    private fun addToggles() {
        val verticesToggle = CheckBox("Vertices", 160f, 0f, 60f, 24f)
        val wireframeToggle = CheckBox("Wireframe", 220f, 0f, 80f, 24f)

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

        add(verticesToggle)
        add(wireframeToggle)
    }

    private fun addSearch() {
        val search = TextInput("Search", 5f, 5f, 150f, 15f)
        search.listenerMap.addListener(KeyEvent::class.java) { event ->
            if (event.action == GLFW.GLFW_RELEASE && loaded) {
                val range = (0..max).toList()
                val filtered = range.filter { it.toString().startsWith(search.textState.text) }

                for (i in 0 until max) {
                    val button = buttons[i]
                    when {
                        filtered.size == max -> { // Reset as no matches
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
        val list = ScrollablePanel(5f, 27f, 150f, 465f)

        max = context.getMaxModels()
        println("Loading models...")

        val x = 2f
        val y = 2f
        val yOffset = 13
        list.container.setSize(142f, y + max * yOffset)

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
            }
            loaded = true
            println("Loaded $max models")
        }
    }
}