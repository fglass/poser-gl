package animation.command.impl

import animation.command.Command
import render.RenderContext

class CopyKeyframeCommand(private val context: RenderContext) : Command {

    override fun execute(): Boolean {
        val animation = context.animationHandler.currentAnimation ?: return false
        val index = animation.getFrameIndex(context.animationHandler.frameCount)
        val keyframe = animation.keyframes[index]
        context.animationHandler.copiedFrame = keyframe
        return true
    }

    override fun unexecute() { }

    override fun reversible() = false
}
