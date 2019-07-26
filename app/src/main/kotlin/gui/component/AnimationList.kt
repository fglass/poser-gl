package gui.component

import Processor
import animation.Animation
import org.liquidengine.legui.style.color.ColorConstants

class AnimationList(private val context: Processor): ElementList() {

    private val elements = HashMap<Int, Element>()
    private var selected: AnimationElement? = null

    init {
        var index = 0
        for (animation in context.cacheService.animations.values) {
            val element = AnimationElement(animation, context, listX, listY + index++ * listYOffset)
            element.addClickListener()

            elements[animation.sequence.id] = element
            container.add(element)
        }
        container.setSize(containerX, listY + index * listYOffset)
    }

    fun addElement(animation: Animation) {
        search("") // Reset search
        val element = AnimationElement(animation, context, listX, container.size.y)
        element.addClickListener()

        elements[animation.sequence.id] = element
        container.add(element)

        container.size.y += listYOffset
        verticalScrollBar.curValue = container.size.y // Scroll to bottom
    }

    fun updateElement(animation: Animation?) {
        if (animation != null) {
            elements[animation.sequence.id]?.updateText()
        }
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
        val animation = context.cacheService.animations[index]?: return
        if (element is AnimationElement) {
            element.animation = animation
            element.updateText()
        }
    }

    class AnimationElement(var animation: Animation, private val context: Processor, x: Float, y: Float):
                           Element(x, y) {
        init {
            updateText()
        }

        override fun updateText() {
            style.background.color = if (context.animationHandler.currentAnimation == animation) {
                hoveredStyle.background.color // Selected
            } else {
                ColorConstants.darkGray()
            }
            textState.text = animation.sequence.id.toString()
            textState.textColor = if (animation.modified) ColorConstants.lightRed() else ColorConstants.white()
            isEnabled = true
        }

        override fun onClickEvent() {
            context.animationHandler.load(animation)
            updateText()
            context.gui.listPanel.animationList.selected?.updateText() // Unselect previous
            context.gui.listPanel.animationList.selected = this
        }
    }
}