package animation

import api.animation.TransformationType
import api.definition.ModelDefinition
import cache.isHigherRev
import entity.HIGHER_REV_SCALE
import org.joml.Vector3f
import java.util.*

class ReferenceNode(transformation: Transformation): Transformation(transformation) {

    var parent: ReferenceNode? = null
    var children = LinkedHashMap<TransformationType, Transformation>()
    var position = Vector3f(0f, 0f, 0f)
    var highlighted = false

    init {
        if (transformation is ReferenceNode) {
            parent = transformation.parent
            position = transformation.position
            highlighted = transformation.highlighted

            for (tf in transformation.children.values) {
                val newTransformation = Transformation(tf)
                children[tf.type] = newTransformation
            }
        }
    }

    fun hasType(type: TransformationType): Boolean {
        return children.containsKey(type)
    }

    fun getTransformation(type: TransformationType): Transformation? {
        return if (type == TransformationType.REFERENCE) this else children[type]
    }

    fun getRotation(): Transformation? {
        return children[TransformationType.ROTATION]
    }

    fun setPosition(def: ModelDefinition) {
        var index = 0f
        val position = Vector3f(delta)

        // Average position over encompassing vertices
        for (i in frameMap) {

            if (i >= def.vertexGroups.size) {
                continue
            }

            val vertexGroup = def.vertexGroups[i]
            index += vertexGroup.size

            for (j in vertexGroup) {
                val pos = Vector3f(
                    def.vertexPositionsX[j].toFloat(),
                    def.vertexPositionsY[j].toFloat(),
                    def.vertexPositionsZ[j].toFloat()
                )

                position.add(pos)
            }
        }

        if (index > 0) {
            position.div(index)
        }

        position.x = -position.x // Flip
        val multiplier = if (isHigherRev) HIGHER_REV_SCALE else 1f
        this.position = position.div(multiplier)
    }

    fun trySetParent(node: ReferenceNode) {
        if (id == node.id || getRotation() == null) {
            return
        }

        // Parent only if its rotation frame map is a superset TODO: better heuristic?
        val rotation = node.getRotation() ?: return
        if (!rotation.frameMap.toSet().containsAll(frameMap.toSet())) {
            return
        }

        // Set if does not exist or closer relation (indicated by a smaller superset)
        val parentRotation = parent?.getRotation()
        if (parentRotation == null || rotation.frameMap.size < parentRotation.frameMap.size) {
            parent = node
        }
    }

    override fun equals(other: Any?): Boolean {
        other as ReferenceNode
        return position == other.position
    }

    override fun hashCode(): Int {
        return Objects.hashCode(position)
    }
}