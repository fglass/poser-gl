package animation.command.impl

import animation.Keyframe
import animation.command.Command
import render.RenderContext

const val UNSET = -1

class AddKeyframeCommand(private val context: RenderContext) : Command {

    private var insertedIndex = UNSET

    override fun execute(): Boolean {
        val animation = context.animationHandler.getAnimationOrCopy() ?: return false
        if (insertedIndex == UNSET) {
            insertedIndex = animation.getFrameIndex(context.animationHandler.frameCount) + 1
        }

        val keyframe = Keyframe(animation.keyframes.size, animation.keyframes[insertedIndex - 1]) // Copy of previous

        return animation.insertKeyframe(keyframe, insertedIndex)
    }

    override fun unexecute() {
        val animation = context.animationHandler.currentAnimation ?: return
        animation.removeKeyframeAt(insertedIndex)
    }

    override fun reversible() = true
}
