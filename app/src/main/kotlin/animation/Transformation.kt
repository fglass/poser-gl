package animation

import api.ITransformation
import api.TransformationType
import net.runelite.cache.definitions.ModelDefinition
import org.joml.Vector3i

open class Transformation(override var id: Int, override val type: TransformationType,
                          var frameMap: IntArray, override var delta: Vector3i) : ITransformation {

    constructor(transformation: Transformation): this(
        transformation.id, transformation.type,
        transformation.frameMap, Vector3i(transformation.delta)
    )

    fun apply(def: ModelDefinition) {
        def.animate(type.id, frameMap, delta.x, delta.y, delta.z)
    }
}