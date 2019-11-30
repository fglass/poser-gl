package animation.command.impl

import animation.Keyframe
import animation.command.Command
import gui.component.Dialog
import render.RenderContext

class PasteKeyframeCommand(private val context: RenderContext) : Command {

    private lateinit var insertedKeyframe: Keyframe

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
        val newIndex = context.animationHandler.getCurrentFrameIndex(animation) + 1
        insertedKeyframe = Keyframe(animation.keyframes.size, copied) // Copy here to avoid shared references
        animation.insertKeyframe(insertedKeyframe, newIndex)
    }

    override fun unexecute() {
        val animation = context.animationHandler.currentAnimation ?: return
        animation.removeKeyframe(insertedKeyframe)
    }

    override fun reversible() = true
}
