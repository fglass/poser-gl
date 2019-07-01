package gui.component

import Processor
import animation.Animation
import gui.GuiManager

class AnimationList(x: Float, y: Float, gui: GuiManager, private val context: Processor): ElementList(x, y, gui) {

    private val elements = HashMap<Int, Element>()

    init {
        var index = 0
        for (animation in context.cacheService.animations.values) {
            val element = AnimationElement(animation, context, listX, listY + index++ * listYOffset, containerX - 6, 14f)
            element.addClickListener()
            elements[animation.sequence.id] = element
            container.add(element)
            maxIndex = animation.sequence.id
        }
        container.setSize(containerX, listY + index * listYOffset)
    }

    fun addElement(animation: Animation) { // TODO while searching
        val element = AnimationElement(animation, context, listX, container.size.y, containerX - 6, 14f)
        element.addClickListener()
        elements[animation.sequence.id] = element
        container.add(element)

        maxIndex += 1
        container.size.y += listYOffset
        verticalScrollBar.curValue = container.size.y // Scroll to bottom
    }

    override fun getFiltered(input: String): List<Int> {
        return elements.keys.filter {
            it.toString().contains(input)
        }
    }

    override fun getElements(): HashMap<Int, Element> {
        return elements
    }

    override fun handleElement(index: Int, element: Element) {
        val animation = context.cacheService.animations[index]
        if (animation != null && element is AnimationElement) {
            element.animation = animation
            element.updateText()
        }
    }

    class AnimationElement(var animation: Animation, private val context: Processor, x: Float, y: Float,
                           width: Float, height: Float): Element(x, y, width, height) {
        init {
            updateText()
        }

        override fun updateText() {
            val prefix = if (animation.modified) "*" else ""
            textState.text = "$prefix${animation.sequence.id}"
            isEnabled = true
        }

        override fun onClickEvent() {
            context.animationHandler.load(animation)
        }
    }
}