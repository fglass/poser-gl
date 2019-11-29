package animation.command

import gui.component.Dialog
import render.RenderContext

class DeleteKeyframeCommand(private val context: RenderContext) : AnimationCommand {

    override fun execute() {
        if (keyframes.size <= 1) {
          Dialog("Invalid Operation", "Unable to delete the last keyframe", context, 200f, 70f).display()
          return
        }

        val index = context.animationHandler.getFrameIndex(this)
        keyframes.remove(keyframes[index])
        updateKeyframes()
    }

    override fun unexecute() {
        // TODO: add deleted
    }

    override fun reversible() = false
}
