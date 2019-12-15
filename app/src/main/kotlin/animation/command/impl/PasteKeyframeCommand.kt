package animation.command.impl

import animation.Keyframe
import animation.command.Command
import gui.component.Dialog
import render.RenderContext

class PasteKeyframeCommand(private val context: RenderContext) : Command {

    private var insertedIndex = UNSET

    override fun execute(): Boolean {
        val copied = context.animationHandler.copiedFrame
        if (copied.id == UNSET) {
            return false
        }

        if (copied.frameMap.id != context.animationHandler.currentAnimation?.getFrameMap()?.id) {
            Dialog("Invalid Operation", "Skeletons do not match", context, 200f, 70f).display()
            return false
        }

        val animation = context.animationHandler.getAnimationOrCopy()?: return false

        if (insertedIndex == UNSET) {
            insertedIndex = animation.getFrameIndex(context.animationHandler.frameCount) + 1
        }
        val keyframe = Keyframe(animation.keyframes.size, copied) // Copy here to avoid shared references
        animation.insertKeyframe(keyframe, insertedIndex)
        return true
    }

    override fun unexecute() {
        val animation = context.animationHandler.currentAnimation ?: return
        animation.removeKeyframeAt(insertedIndex)
    }

    override fun reversible() = true
}
