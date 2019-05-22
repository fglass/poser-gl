package gui

import Processor
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.liquidengine.legui.component.*
import org.liquidengine.legui.event.MouseClickEvent


/**
 * @author Fred
 */
class Gui(x: Float, y: Float, width: Float, height: Float, private val context: Processor): Panel(x, y, width, height) {

    fun createElements() {
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

        val search = TextInput("Search", 5f, 5f, 150f, 15f)
        add(search)
        addScrollable()
    }

    private fun addScrollable() {
        val list = ScrollablePanel(5f, 27f, 150f, 465f)

        val max = context.getMaxModels()
        println("Loading $max models...")

        val x = 2f
        val y = 2f
        val yOffset = 13
        list.container.setSize(142f, y + max * yOffset)

        // Asynchronously add labels
        GlobalScope.launch {
            for (i in 0 until max) {
                val label = Button("$i", x, y + i * yOffset, 137f, 10f)

                label.listenerMap.addListener(MouseClickEvent::class.java) { event ->
                    if (MouseClickEvent.MouseClickAction.CLICK == event.action) {
                        context.setModel(i)
                    }
                }

                list.container.add(label)
                remove(list)
                add(list)
            }
        }
    }
}