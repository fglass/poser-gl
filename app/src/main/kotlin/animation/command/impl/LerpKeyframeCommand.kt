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
            Dialog("Invalid Operation", "Insufficient number of keyframes", context, 200f, 70f).display()
            return false
        }

        val index = animation.getFrameIndex(context.animationHandler.frameCount)
        if (index >= animation.keyframes.size - 1) {
            Dialog("Invalid Operation", "No subsequent keyframe to interpolate with", context, 250f, 70f).display()
            return false
        }

        val first = animation.keyframes[index]
        val second = animation.keyframes[index + 1]
        if (first.frameMap.id != second.frameMap.id) {
            Dialog("Invalid Operation", "Skeletons do not match", context, 200f, 70f).display()
            return false
        }

        val largest = if (first.transformations.size > second.transformations.size) first else second
        val smallest = if (largest == first) second else first
        val keyframe = Keyframe(animation.keyframes.size, largest)

        repeat(smallest.transformations.size) {
            val delta = Vector3f(first.transformations[it].delta).lerp(Vector3f(second.transformations[it].delta), 0.5f)
            keyframe.transformations[it].delta = Vector3i(delta.x.toInt(), delta.y.toInt(), delta.z.toInt())
        }

        insertedIndex = index + 1
        context.animationHandler.getAnimationOrCopy()?.insertKeyframe(keyframe, insertedIndex)
        return true
    }

    override fun unexecute() {
        val animation = context.animationHandler.currentAnimation ?: return
        animation.removeKeyframeAt(insertedIndex)
    }

    override fun reversible() = true
}
