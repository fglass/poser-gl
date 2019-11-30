package animation.command.impl

import animation.Keyframe
import animation.command.Command
import render.RenderContext

class AddKeyframeCommand(private val context: RenderContext) : Command {

    override fun execute() {
        val animation = context.animationHandler.getAnimationOrCopy()?: return
        val newIndex = context.animationHandler.getFrameIndex(animation) + 1
        val keyframe = Keyframe(animation.keyframes.size, animation.keyframes[newIndex - 1]) // Copy previous TODO: range checks
        animation.insertKeyframe(newIndex, keyframe)
    }

    override fun unexecute() {
        println("deleting added")
    }

    override fun reversible() = true
}
