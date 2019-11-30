package animation.command

import animation.Keyframe
import gui.component.Dialog
import org.joml.Vector3f
import org.joml.Vector3i
import render.RenderContext

class InterpolateKeyframeCommand(private val context: RenderContext) : Command {

    override fun execute() {
        val animation = context.animationHandler.currentAnimation ?: return
        if (animation.keyframes.size <= 1) {
            Dialog("Invalid Operation", "Insufficient number of keyframes", context, 200f, 70f).display()
            return
        }

        val index = context.animationHandler.getFrameIndex(animation)
        if (index >= animation.keyframes.size - 1) {
            Dialog("Invalid Operation", "No subsequent keyframe to interpolate with", context, 250f, 70f).display()
            return
        }

        val first = animation.keyframes[index]
        val second = animation.keyframes[index + 1]
        if (first.frameMap.id != second.frameMap.id) {
            Dialog("Invalid Operation", "Skeletons do not match", context, 200f, 70f).display()
            return
        }

        val largest = if (first.transformations.size > second.transformations.size) first else second
        val smallest = if (largest == first) second else first
        val interpolated = Keyframe(animation.keyframes.size, largest)

        repeat(smallest.transformations.size) {
            val delta = Vector3f(first.transformations[it].delta).lerp(Vector3f(second.transformations[it].delta), 0.5f)
            interpolated.transformations[it].delta = Vector3i(delta.x.toInt(), delta.y.toInt(), delta.z.toInt())
        }
        context.animationHandler.getAnimationOrCopy()?.insertKeyframe(index + 1, interpolated)
    }

    override fun unexecute() {
        // TODO: delete inserted
    }

    override fun reversible() = false
}
