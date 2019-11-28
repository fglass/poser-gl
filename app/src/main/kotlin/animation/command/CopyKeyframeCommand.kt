package animation.command

import gui.component.Dialog
import render.RenderContext

class CopyKeyframeCommand(private val context: RenderContext) : KeyframeCommand {

    override fun execute() {
        val animation = context.animationHandler.getAnimation(useCurrent = true)?: return
        val index = context.animationHandler.getFrameIndex(animation)
        val keyframe = animation.keyframes[index]
        context.animationHandler.copiedFrame = keyframe
        Dialog("Keyframe Action", "Successfully copied keyframe ${keyframe.id}", context, 200f, 70f).display()
    }

    override fun unexecute() { }

    override fun reversible() = false
}