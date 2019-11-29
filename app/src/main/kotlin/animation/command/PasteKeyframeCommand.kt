package animation.command

import animation.Keyframe
import gui.component.Dialog
import render.RenderContext

class PasteKeyframeCommand(private val context: RenderContext) : AnimationCommand {

    override fun execute() {
        val copied = context.animationHandler.copiedFrame
        if (copied.id == -1) {
            Dialog("Invalid Operation", "You have no keyframe copied", context, 200f, 70f).display()
            return
        }

        if (copied.frameMap.id != context.animationHandler.currentAnimation?.getFrameMap()?.id) {
            Dialog("Invalid Operation", "Skeletons do not match", context, 200f, 70f).display()
            return
        }

        val animation = context.animationHandler.getAnimation()?: return
        val keyframe = Keyframe(animation.keyframes.size, copied) // Copy after to avoid shared references
        val newIndex = context.animationHandler.getFrameIndex(animation) + 1
        animation.insertKeyframe(newIndex, keyframe)
    }

    override fun unexecute() {
        // TODO: delete inserted
    }

    override fun reversible() = true
}
