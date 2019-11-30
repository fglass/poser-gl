package animation.command.impl

import animation.Keyframe
import animation.command.Command
import render.RenderContext

class AddKeyframeCommand(private val context: RenderContext) : Command {

    private lateinit var insertedKeyframe: Keyframe

    override fun execute() {
        val animation = context.animationHandler.getAnimationOrCopy() ?: return
        val newIndex = context.animationHandler.getCurrentFrameIndex(animation) + 1

        insertedKeyframe = Keyframe(animation.keyframes.size, animation.keyframes[newIndex - 1]) // Copy of previous
        animation.insertKeyframe(insertedKeyframe, newIndex)
    }

    override fun unexecute() {
        val animation = context.animationHandler.currentAnimation ?: return
        animation.removeKeyframe(insertedKeyframe)
    }

    override fun reversible() = true
}
