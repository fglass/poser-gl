package animation.command.impl

import animation.Keyframe
import animation.command.Command
import render.RenderContext

const val UNSET = -1

class AddKeyframeCommand(private val context: RenderContext) : Command {

    private var insertedIndex = UNSET

    override fun execute() {
        val animation = context.animationHandler.getAnimationOrCopy() ?: return
        if (insertedIndex == UNSET) {
            insertedIndex = context.animationHandler.getCurrentFrameIndex(animation) + 1
        }

        val keyframe = Keyframe(animation.keyframes.size, animation.keyframes[insertedIndex - 1]) // Copy previous TODO: range checks
        animation.insertKeyframe(keyframe, insertedIndex)
    }

    override fun unexecute() {
        val animation = context.animationHandler.currentAnimation ?: return
        animation.removeKeyframeAt(insertedIndex)
    }

    override fun reversible() = true
}
