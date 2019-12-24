package gui.component

import api.definition.ItemDefinition
import render.RenderContext

class ItemList(context: RenderContext): ElementList() {

    private val items = context.cacheService.items
    private val elements = HashMap<Int, Element>()

    init {
        var index = 0
        for (item in items.values) {
            val element = ItemElement(item, context, listX, listY + index++ * listYOffset)
            element.addClickListener()
            elements[item.id] = element
            container.add(element)
        }
        container.setSize(containerX, listY + index * listYOffset)
    }

    override fun getFiltered(input: String): List<Int> {
        return elements.keys.filter {
            val item = items[it]
            item != null && item.name.toLowerCase().contains(input)
        }
    }

    override fun getElements(): HashMap<Int, Element> {
        return elements
    }

    override fun handleElement(index: Int, element: Element) {
        val item = items[index]?: return
        if (element is ItemElement) {
            element.item = item
            element.updateText()
        }
    }

    class ItemElement(var item: ItemDefinition, private val context: RenderContext, x: Float, y: Float): Element(x, y) {
        init {
            updateText()
        }

        override fun updateText() {
            textState.text = item.name
            isEnabled = true
        }

        override fun onClickEvent() {
            context.entityHandler.entity?.addItem(item, context.entityHandler)
        }
    }
}