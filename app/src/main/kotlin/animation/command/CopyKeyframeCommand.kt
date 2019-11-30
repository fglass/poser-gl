package animation.command

import render.RenderContext

class CopyKeyframeCommand(private val context: RenderContext) : Command {

    override fun execute() {
        val animation = context.animationHandler.currentAnimation ?: return
        val index = context.animationHandler.getFrameIndex(animation)
        val keyframe = animation.keyframes[index]
        context.animationHandler.copiedFrame = keyframe
    }

    override fun unexecute() { }

    override fun reversible() = false
}