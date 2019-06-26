package animation.reference

import animation.Reference
import org.joml.Vector3f
import java.util.*

class Node(val reference: Reference, val position: Vector3f) {

    val scale = 2.5f
    var highlighted = false

    fun isToggled(selected: Node?): Boolean {
        if (selected == null) {
            return false
        }
        return selected.reference.id == reference.id
    }

    override fun equals(other: Any?): Boolean {
        other as Node
        return position == other.position
    }

    override fun hashCode(): Int {
        return Objects.hashCode(position)
    }
}