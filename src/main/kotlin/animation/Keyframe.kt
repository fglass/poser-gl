package animation

import Processor
import net.runelite.cache.definitions.ModelDefinition.*
import shader.ShadingType

class Keyframe(val id: Int, var length: Int) {

    // Copy constructor
    constructor(newId: Int, keyframe: Keyframe): this(newId, keyframe.length) {
        keyframe.transformations.forEach {
            if (it is Reference) {
                val newReference = Reference(it)
                this.transformations.add(newReference)

                for (transformation in it.group.values) {
                    if (transformation.type == TransformationType.REFERENCE) {
                        continue
                    }
                    val newTransformation = Transformation(transformation)
                    newReference.group[transformation.type] = newTransformation
                    this.transformations.add(newTransformation)
                }
            }
        }
    }

    val transformations = ArrayList<Transformation>()

    fun add(transformation: Transformation, id: Int) {
        transformation.id = id
        transformations.add(transformation)
    }

    fun apply(context: Processor) {
        // Reset from last frame
        context.framebuffer.nodeRenderer.nodes.clear()
        animOffsetX = 0
        animOffsetY = 0
        animOffsetZ = 0

        val entity = context.entity ?: return
        val def = entity.model.definition
        def.resetAnim()

        for (transformation in transformations) {
            if (transformation is Reference) {
                context.framebuffer.nodeRenderer.addNode(def, transformation)
            }
            transformation.apply(def)
        }

        // Load transformed model
        context.loader.cleanUp()
        entity.model = context.datLoader.parse(def, context.framebuffer.shadingType == ShadingType.FLAT)
    }

    fun changeLength(newLength: Int, context: Processor) {
        length = newLength
        context.animationHandler.restartFrame()
        context.gui.animationPanel.setTimeline()
    }
}