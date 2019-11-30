package animation.command.impl

import animation.Keyframe
import animation.command.Command
import gui.component.Dialog
import render.RenderContext

class DeleteKeyframeCommand(private val context: RenderContext) : Command {

    private lateinit var deletedKeyframe: Keyframe
    private var removedIndex = -1

    override fun execute() {
        var animation = context.animationHandler.currentAnimation ?: return
        if (animation.keyframes.size <= 1) {
          Dialog("Invalid Operation", "Unable to delete the last keyframe", context, 200f, 70f).display()
          return
        }

        animation = context.animationHandler.getAnimationOrCopy() ?: return
        if (removedIndex == -1) {
            removedIndex = context.animationHandler.getCurrentFrameIndex(animation)
        }

        deletedKeyframe = animation.keyframes[removedIndex]
        animation.removeKeyframe(deletedKeyframe)
    }

    override fun unexecute() {
        val animation = context.animationHandler.currentAnimation ?: return
        animation.insertKeyframe(deletedKeyframe, removedIndex)
    }

    override fun reversible() = true
}
