package animation.command

import render.RenderContext

class AddKeyframeCommand(private val context: RenderContext) : AnimationCommand {

    override fun execute() {
        val animation = context.animationHandler.getAnimation(useCurrent = true)?: return
        val newIndex = context.animationHandler.getFrameIndex(this) + 1
        val keyframe = Keyframe(keyframes.size, keyframes[newIndex - 1]) // Copy previous TODO: range checks
        insertKeyframe(newIndex, keyframe)
    }

    override fun unexecute() {
        // TODO: delete added
    }

    override fun reversible() = false
}
