package animation.command.impl

import animation.Keyframe
import animation.command.Command
import gui.component.Dialog
import org.joml.Vector3f
import org.joml.Vector3i
import render.RenderContext

class LerpKeyframeCommand(private val context: RenderContext) : Command {

    private var insertedIndex = UNSET

    override fun execute(): Boolean {
        val animation = context.animationHandler.currentAnimation ?: return false

        if (animation.keyframes.size <= 1) {
            displayError("Insufficient number of keyframes")
            return false
        }

        val index = animation.getFrameIndex(context.animationHandler.frameCount)
        val firstKeyframe = animation.keyframes[index]

        val nextIndex = (index + 1).rem(animation.keyframes.size)
        val secondKeyframe = animation.keyframes[nextIndex]

        if (firstKeyframe.frameMap.id != secondKeyframe.frameMap.id) {
            displayError("Skeletons do not match")
            return false
        }

        val largest = if (firstKeyframe.transformations.size > secondKeyframe.transformations.size) firstKeyframe else secondKeyframe
        val smallest = if (largest == firstKeyframe) secondKeyframe else firstKeyframe
        val newKeyframe = Keyframe(animation.keyframes.size, largest)

        repeat(smallest.transformations.size) {
            val delta = Vector3f(firstKeyframe.transformations[it].delta).lerp(Vector3f(secondKeyframe.transformations[it].delta), 0.5f)
            newKeyframe.transformations[it].delta = Vector3i(delta.x.toInt(), delta.y.toInt(), delta.z.toInt())
        }

        insertedIndex = index + 1
        context.animationHandler.getAnimationOrCopy()?.insertKeyframe(newKeyframe, insertedIndex)

        return true
    }

    private fun displayError(message: String) {
        Dialog("Invalid Operation", message, context, 200f, 70f).display()

    }

    override fun unexecute() {
        val animation = context.animationHandler.currentAnimation ?: return
        animation.removeKeyframeAt(insertedIndex)
    }

    override fun reversible() = true
}
