package animation

import org.joml.Vector3f
import java.util.*

class ReferenceNode(val reference: Reference, val position: Vector3f) {

    val scale = 2.5f
    var highlighted = false

    fun isToggled(selected: ReferenceNode?): Boolean {
        if (selected == null) {
            return false
        }
        return selected.reference.id == reference.id
    }

    override fun equals(other: Any?): Boolean {
        other as ReferenceNode
        return position == other.position
    }

    override fun hashCode(): Int {
        return Objects.hashCode(position)
    }
}