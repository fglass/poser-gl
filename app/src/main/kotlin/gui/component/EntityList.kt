package gui.component

import api.definition.NpcDefinition
import render.RenderContext

class EntityList(context: RenderContext): ElementList() {

    private val entities = context.cacheService.entities
    private val elements = HashMap<Int, Element>()

    init {
        var index = 0
        for (entity in entities.values.sortedBy { it.id }) {
            val element = EntityElement(entity, context, listX, listY + index++ * listYOffset)
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

    class EntityElement(var entity: NpcDefinition, private val context: RenderContext, x: Float, y: Float): Element(x, y) {
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