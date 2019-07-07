package animation

import net.runelite.cache.definitions.ModelDefinition
import org.joml.Vector3i

open class Transformation(var id: Int, val type: TransformationType, var frameMap: IntArray, var delta: Vector3i) {

    constructor(transformation: Transformation) : this(
        transformation.id, transformation.type,
        transformation.frameMap, Vector3i(transformation.delta)
    )

    fun apply(def: ModelDefinition) {
        def.animate(type.id, frameMap, delta.x, delta.y, delta.z)
    }
}