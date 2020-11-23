package animation

import api.animation.IKeyframe
import api.definition.FrameMapDefinition
import api.definition.ModelDefinition
import api.definition.ModelDefinition.Companion.animOffsetX
import api.definition.ModelDefinition.Companion.animOffsetY
import api.definition.ModelDefinition.Companion.animOffsetZ
import entity.Entity
import render.RenderContext
import shader.ShadingType

class Keyframe(
    val id: Int = -1,
    val frameId: Int = -1,
    var length: Int = -1,
    override val frameMap: FrameMapDefinition = FrameMapDefinition()
) : IKeyframe {

    // Copy constructor
    constructor(newId: Int, keyframe: Keyframe): this(newId, keyframe.frameId, keyframe.length, keyframe.frameMap) {
        modified = keyframe.modified
        keyframe.transformations.forEach {
            if (it is ReferenceNode) {
                val newReference = ReferenceNode(it)
                transformations.add(newReference)
                newReference.children.forEach { child -> transformations.add(child.value) }
            }
        }
    }

    override var modified = false
    override val transformations = ArrayList<Transformation>()

    fun apply(context: RenderContext) {
        context.nodeRenderer.nodes.clear()

        val entity = context.entityHandler.entity ?: return
        val def = entity.model.definition
        def.resetTransformations()

        transformations.forEach { it.apply(def) }

        transformations.filterIsInstance<ReferenceNode>().forEach {
            context.nodeRenderer.addNode(it, def) // After all transformations applied
        }

        loadTransformedModel(context, entity, def)
    }

    private fun ModelDefinition.resetTransformations() {
        animOffsetX = 0
        animOffsetY = 0
        animOffsetZ = 0

        origVX?.let { System.arraycopy(it, 0, this.vertexPositionsX, 0, it.size) }
        origVY?.let { System.arraycopy(it, 0, this.vertexPositionsY, 0, it.size) }
        origVZ?.let { System.arraycopy(it, 0, this.vertexPositionsZ, 0, it.size) }
    }

    private fun loadTransformedModel(context: RenderContext, entity: Entity, def: ModelDefinition) {
        context.modelParser.cleanUp()
        val useFlatShading = context.framebuffer.shadingType == ShadingType.FLAT
        entity.model = context.modelParser.parse(def, useFlatShading)
    }
}