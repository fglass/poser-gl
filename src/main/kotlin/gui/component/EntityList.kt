package gui.component

import Processor
import gui.Gui
import net.runelite.cache.definitions.NpcDefinition

class EntityList(x: Float, y: Float, gui: Gui, context: Processor): ElementList(x, y, gui) {

    private val entities = context.entityLoader.entities
    private val entityElements = mutableListOf<EntityElement>()

    init {
        var index = 0
        for ((i, npc) in entities.withIndex()) {
            val element = EntityElement(npc, context, listX, listY + index++ * listYOffset, 137f, 14f)
            element.addClickListener()
            entityElements.add(element)
            container.add(element)
            maxIndex = i
        }
        container.setSize(142f, listY + index * listYOffset)
        println("Loaded $index npcs")
    }

    override fun getFiltered(input: String): List<Int> {
        return (0 until maxIndex).toList().filter {
            entities[it].name.toLowerCase().contains(input)
        }
    }

    override fun getElements(): List<Element> {
        return entityElements
    }

    override fun handleElement(index: Int, element: Element) {
        val entity = entities[index]
        if (element is EntityElement) {
            element.npc = entity
            element.updateText()
        }
    }

    class EntityElement(var npc: NpcDefinition, private val context: Processor, x: Float, y: Float,
                        width: Float, height: Float): Element(x, y, width, height) {
        init {
            updateText()
        }

        override fun updateText() {
            textState.text = npc.name
            isEnabled = true
        }

        override fun onClickEvent() {
            context.entityLoader.load(npc)
        }
    }
}