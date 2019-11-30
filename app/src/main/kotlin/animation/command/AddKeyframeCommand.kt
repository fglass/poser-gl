package animation.command

import animation.Keyframe
import render.RenderContext

class AddKeyframeCommand(private val context: RenderContext) : Command {

    override fun execute() {
        val animation = context.animationHandler.getAnimationOrCopy()?: return
        val newIndex = context.animationHandler.getFrameIndex(animation) + 1
        val keyframe = Keyframe(animation.keyframes.size, animation.keyframes[newIndex - 1]) // Copy previous TODO: range checks
        animation.insertKeyframe(newIndex, keyframe)
    }

    override fun unexecute() {
        // TODO: delete added
    }

    override fun reversible() = false
}
