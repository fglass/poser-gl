package gui.component

import Processor
import gui.Gui
import net.runelite.cache.definitions.ItemDefinition

class ItemList(x: Float, y: Float, gui: Gui, context: Processor): ElementList(x, y, gui) {

    private val items = context.itemLoader.items
    private val itemElements = mutableListOf<ItemElement>()

    init {
        var index = 0
        for ((i, item) in items.withIndex()) {
            val element = ItemElement(item, context, listX, listY + index++ * listYOffset, containerX - 6, 14f)
            element.addClickListener()
            itemElements.add(element)
            container.add(element)
            maxIndex = i
        }
        container.setSize(containerX, listY + index * listYOffset)
        println("Loaded $index items")
    }

    override fun getFiltered(input: String): List<Int> {
        return (0 until maxIndex).toList().filter {
            items[it].name.toLowerCase().contains(input)
        }
    }

    override fun getElements(): List<Element> {
        return itemElements
    }

    override fun handleElement(index: Int, element: Element) {
        val item = items[index]
        if (element is ItemElement) {
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
            context.entity?.add(models, context.entityLoader)
        }
    }
}