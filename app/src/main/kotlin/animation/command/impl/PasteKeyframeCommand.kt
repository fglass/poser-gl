package animation.command.impl

import animation.Keyframe
import animation.command.Command
import gui.component.Dialog
import render.RenderContext

class PasteKeyframeCommand(private val context: RenderContext) : Command {

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
        val keyframe = Keyframe(animation.keyframes.size, copied) // Copy after to avoid shared references
        val newIndex = context.animationHandler.getCurrentFrameIndex(animation) + 1
        animation.insertKeyframe(keyframe, newIndex)
    }

    override fun unexecute() {
        println("deleting pasted")
    }

    override fun reversible() = true
}
