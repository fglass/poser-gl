package gui.component

import Processor
import gui.GuiManager
import net.runelite.cache.definitions.NpcDefinition

class EntityList(x: Float, y: Float, gui: GuiManager, context: Processor): ElementList(x, y, gui) {

    private val entities = context.cacheService.entities
    private val elements = HashMap<Int, Element>()

    init {
        var index = 0
        for (entity in entities.values) {
            val element = EntityElement(entity, context, listX, listY + index++ * listYOffset, containerX - 6, 14f)
            element.addClickListener()
            elements[entity.id] = element
            container.add(element)
        }
        container.setSize(containerX, listY + index * listYOffset)
    }

    override fun getFiltered(input: String): List<Int> {
        return elements.keys.filter {
            val entity = entities[it]
            entity != null && entity.name.toLowerCase().contains(input)
        }
    }

    override fun getElements(): HashMap<Int, Element> {
        return elements
    }

    override fun handleElement(index: Int, element: Element) {
        val entity = entities[index]?: return
        if (element is EntityElement) {
            element.entity = entity
            element.updateText()
        }
    }

    class EntityElement(var entity: NpcDefinition, private val context: Processor, x: Float, y: Float,
                        width: Float, height: Float): Element(x, y, width, height) {
        init {
            updateText()
        }

        override fun updateText() {
            textState.text = entity.name
            isEnabled = true
        }

        override fun onClickEvent() {
            context.entityHandler.load(entity)
        }
    }
}