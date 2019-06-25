package animation.joint

import org.joml.Vector3f
import java.util.*

class Joint(val position: Vector3f) {

    val scale = 2.5f
    var highlighted = false

    override fun equals(other: Any?): Boolean {
        other as Joint
        return position == other.position
    }

    override fun hashCode(): Int {
        return Objects.hashCode(position)
    }
}