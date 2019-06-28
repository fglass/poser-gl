package animation

import net.runelite.cache.definitions.ModelDefinition.*
import shader.ShadingType
import Processor
import org.joml.Vector3i

class Keyframe(val id: Int, var length: Int) {

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

    fun copy(newId: Int): Keyframe {
        val newKeyframe = Keyframe(newId, length)
        transformations.forEach { // Replace transformation references
            val newTransformation: Transformation

            if (it is Reference)  {
                newTransformation = Reference(it)
                newTransformation.group = it.group
            } else {
                newTransformation = Transformation(it)
            }

            newTransformation.offset = Vector3i(it.offset)
            newKeyframe.transformations.add(newTransformation)
        }
        return newKeyframe
    }
}