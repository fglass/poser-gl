package gui.component

import Processor
import animation.Animation
import gui.GuiManager

class AnimationList(x: Float, y: Float, gui: GuiManager, private val context: Processor): ElementList(x, y, gui) {

    private val animationElements = mutableListOf<AnimationElement>()

    init {
        var index = 0
        for ((i, animation) in context.animationHandler.animations.values.withIndex()) {
            val element = AnimationElement(animation, context, listX, listY + index++ * listYOffset, containerX - 6, 14f)
            element.addClickListener()
            animationElements.add(element)
            container.add(element)
            maxIndex = i
        }
        container.setSize(containerX, listY + index * listYOffset)
    }

    fun addElement(animation: Animation) {
        val element = AnimationElement(animation, context, listX, container.size.y, containerX - 6, 14f)
        element.addClickListener()
        animationElements.add(element)
        container.add(element)

        maxIndex += 1
        container.size.y += listYOffset
        verticalScrollBar.curValue = container.size.y // Scroll to bottom
    }

    override fun getFiltered(input: String): List<Int> {
        return (0..maxIndex).toList().filter {
            it.toString().contains(input)
        }
    }

    override fun getElements(): List<Element> {
        return animationElements
    }

    override fun handleElement(index: Int, element: Element) {
        val animation = context.animationHandler.animations[index]
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