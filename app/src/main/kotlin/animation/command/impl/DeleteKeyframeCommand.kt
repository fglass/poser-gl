package animation.command.impl

import animation.Keyframe
import animation.command.Command
import gui.component.Dialog
import render.RenderContext

class DeleteKeyframeCommand(private val context: RenderContext) : Command {

    private lateinit var deletedKeyframe: Keyframe
    private var removedIndex = UNSET

    override fun execute(): Boolean {
        var animation = context.animationHandler.currentAnimation ?: return false
        if (animation.keyframes.size <= 1) {
            Dialog("Invalid Operation", "Unable to delete the last keyframe", context, 200f, 70f).display()
            return false
        }

        animation = context.animationHandler.getAnimationOrCopy() ?: return false
        if (removedIndex == UNSET) {
            removedIndex = animation.getFrameIndex(context.animationHandler.frameCount)
        }

        deletedKeyframe = animation.keyframes[removedIndex]
        animation.removeKeyframe(deletedKeyframe)
        return true
    }

    override fun unexecute() {
        val animation = context.animationHandler.currentAnimation ?: return
        animation.insertKeyframe(deletedKeyframe, removedIndex)
    }

    override fun reversible() = true
}
