package animation.command.impl

import animation.Keyframe
import animation.command.Command
import gui.component.Dialog
import render.RenderContext

class PasteKeyframeCommand(private val context: RenderContext) : Command {

    private var insertedIndex = UNSET

    override fun execute() {
        val copied = context.animationHandler.copiedFrame
        if (copied.id == -1) {
            return
        }

        if (copied.frameMap.id != context.animationHandler.currentAnimation?.getFrameMap()?.id) {
            Dialog("Invalid Operation", "Skeletons do not match", context, 200f, 70f).display()
            return
        }

        val animation = context.animationHandler.getAnimationOrCopy()?: return
        if (insertedIndex == UNSET) {
            insertedIndex = context.animationHandler.getCurrentFrameIndex(animation) + 1
        }

        val keyframe = Keyframe(animation.keyframes.size, copied) // Copy here to avoid shared references
        animation.insertKeyframe(keyframe, insertedIndex)
    }

    override fun unexecute() {
        val animation = context.animationHandler.currentAnimation ?: return
        animation.removeKeyframeAt(insertedIndex)
    }

    override fun reversible() = true
}
