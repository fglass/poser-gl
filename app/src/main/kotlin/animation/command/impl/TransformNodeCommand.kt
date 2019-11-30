package animation.command.impl

import animation.Transformation
import animation.command.Command
import mu.KotlinLogging
import render.RenderContext

private val logger = KotlinLogging.logger {}

class TransformNodeCommand(private val context: RenderContext, private val coordIndex: Int, private val value: Int) :
      Command {

    private lateinit var transformation: Transformation
    private var previousValue: Int? = null

    override fun execute(): Boolean {
        if (!context.nodeRenderer.enabled) {
            return false
        }

        val selected = context.nodeRenderer.selectedNode ?: return false
        val type = context.nodeRenderer.selectedType
        val preCopy = selected.getTransformation(type) ?: return false

        val animation = context.animationHandler.getAnimationOrCopy() ?: return false
        val index = context.animationHandler.getCurrentFrameIndex(animation)
        val keyframe = animation.keyframes[index]

        try {
            transformation = keyframe.transformations.first { it.id == preCopy.id }
            if (previousValue == null) {
                previousValue = transformation.delta.get(coordIndex)
            }
            transform(value)
            keyframe.modified = true
        } catch (e: NoSuchElementException) {
            logger.error(e) { "Node ${preCopy.id} does not exist" }
            return false
        }
        return true
    }

    override fun unexecute() {
        previousValue?.let { transform(it) }
    }

    private fun transform(value: Int) {
        transformation.delta.setComponent(coordIndex, value)
        context.gui.editorPanel.sliders[coordIndex].setValue(value)
    }

    override fun reversible() = true
}