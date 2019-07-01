package gui.component

import Processor
import gui.GuiManager
import net.runelite.cache.definitions.ItemDefinition

class ItemList(x: Float, y: Float, gui: GuiManager, context: Processor): ElementList(x, y, gui) {

    private val items = context.cacheService.items
    private val elements = HashMap<Int, Element>()

    init {
        var index = 0
        for (item in items.values) {
            val element = ItemElement(item, context, listX, listY + index++ * listYOffset, containerX - 6, 14f)
            element.addClickListener()
            elements[item.id] = element
            container.add(element)
            maxIndex = item.id
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
        val item = items[index]
        if (item != null && element is ItemElement) {
            element.item = item
            element.updateText()
        }
    }

    class ItemElement(var item: ItemDefinition, private val context: Processor, x: Float, y: Float,
                           width: Float, height: Float): Element(x, y, width, height) {
        init {
            updateText()
        }

        override fun updateText() {
            textState.text = item.name
            isEnabled = true
        }

        override fun onClickEvent() {
            val models = intArrayOf(item.maleModel0, item.maleModel1, item.maleModel2)
            context.entity?.add(models, context.entityHandler)
        }
    }
}