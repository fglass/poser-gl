package animation

import net.runelite.cache.definitions.ModelDefinition
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

    fun getPosition(def: ModelDefinition): Vector3f {
        var index = 0f
        val position = Vector3f(delta)

        // Average position over encompassing vertices
        for (i in frameMap) {
            if (i < def.vertexGroups.size) {
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
        }

        if (index > 0) {
            position.div(index)
        }

        position.x = -position.x // Flip
        return position
    }

    fun isToggled(selected: ReferenceNode?): Boolean {
        if (selected == null) {
            return false
        }
        return selected.id == id
    }
}