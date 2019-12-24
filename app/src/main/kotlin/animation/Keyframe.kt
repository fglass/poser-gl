package animation

import api.animation.IKeyframe
import api.definition.FrameMapDefinition
import api.definition.ModelDefinition
import api.definition.ModelDefinition.Companion.animOffsetX
import api.definition.ModelDefinition.Companion.animOffsetY
import api.definition.ModelDefinition.Companion.animOffsetZ
import render.RenderContext
import shader.ShadingType

class Keyframe(val id: Int = -1, val frameId: Int = -1, var length: Int = -1,
               override val frameMap: FrameMapDefinition = FrameMapDefinition()) : IKeyframe {

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
        // Reset from last frame
        context.nodeRenderer.nodes.clear()
        animOffsetX = 0
        animOffsetY = 0
        animOffsetZ = 0

        val entity = context.entity?: return
        val def = entity.model.definition
        def.resetAnim()

        for (transformation in transformations) {
            if (transformation is ReferenceNode) {
                context.nodeRenderer.addNode(transformation, def)
            }
            transformation.apply(def)
        }

        // Load transformed model
        context.modelParser.cleanUp()
        entity.model = context.modelParser.parse(def, context.framebuffer.shadingType == ShadingType.FLAT)
    }

    private fun ModelDefinition.resetAnim() {
        if (origVX != null) {
            System.arraycopy(origVX!!, 0, this.vertexPositionsX, 0, origVX!!.size)
            System.arraycopy(origVY!!, 0, this.vertexPositionsY, 0, origVY!!.size)
            System.arraycopy(origVZ!!, 0, this.vertexPositionsZ, 0, origVZ!!.size)
        }
    }
}