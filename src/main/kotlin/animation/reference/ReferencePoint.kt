package animation.reference

import org.joml.Vector3f
import java.util.*

class ReferencePoint(val position: Vector3f) {

    val scale = 2.5f
    var highlighted = false

    override fun equals(other: Any?): Boolean {
        other as ReferencePoint
        return position == other.position
    }

    override fun hashCode(): Int {
        return Objects.hashCode(position)
    }
}