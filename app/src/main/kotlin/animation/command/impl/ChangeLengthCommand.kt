package animation.command.impl

import animation.Animation
import animation.command.Command
import render.RenderContext

class ChangeLengthCommand(private val context: RenderContext, private val length: Int) : Command {

    private var index = UNSET
    private var previousLength = UNSET

    override fun execute(): Boolean {
        val animation = context.animationHandler.getAnimationOrCopy() ?: return false
        if (index == UNSET) {
            index = animation.getFrameIndex(context.animationHandler.frameCount)
        }

        val keyframe = animation.keyframes[index]
        previousLength = keyframe.length
        keyframe.length = length
        keyframe.modified = true
        context.gui.editorPanel.frameLength.setValue(length) // For redo
        animation.update()
        return true
    }

    override fun unexecute() {
        val animation = context.animationHandler.currentAnimation ?: return
        animation.keyframes[index].length = previousLength
        context.gui.editorPanel.frameLength.setValue(previousLength)
        animation.update()
    }

    private fun Animation.update() {
        length = calculateLength()
        context.animationHandler.setCurrentFrame(context.animationHandler.frameCount) // Restart frame
        context.gui.animationPanel.setTimeline()
    }

    override fun reversible() = true
}