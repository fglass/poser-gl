package animation.command.impl

import animation.command.Command
import gui.component.Dialog
import render.RenderContext

class DeleteKeyframeCommand(private val context: RenderContext) : Command {

    override fun execute() {
        var animation = context.animationHandler.currentAnimation ?: return
        if (animation.keyframes.size <= 1) {
          Dialog("Invalid Operation", "Unable to delete the last keyframe", context, 200f, 70f).display()
          return
        }

        animation = context.animationHandler.getAnimationOrCopy() ?: return
        val index = context.animationHandler.getFrameIndex(animation)
        animation.keyframes.remove(animation.keyframes[index])
        animation.updateKeyframes()
    }

    override fun unexecute() {
        println("adding deleted")
    }

    override fun reversible() = true
}
