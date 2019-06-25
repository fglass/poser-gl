package animation.reference

import animation.AnimationHandler
import org.joml.Vector3f
import java.util.*

class ReferenceNode(val transformation: AnimationHandler.Transformation, val position: Vector3f) {

    val scale = 2.5f
    var highlighted = false

    override fun equals(other: Any?): Boolean {
        other as ReferenceNode
        return position == other.position
    }

    override fun hashCode(): Int {
        return Objects.hashCode(position)
    }
}