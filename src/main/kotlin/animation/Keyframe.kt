package animation

import Processor
import net.runelite.cache.definitions.ModelDefinition.*
import shader.ShadingType
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

class Keyframe(val id: Int, val frameId: Int, var length: Int) {

    // Copy constructor
    constructor(newId: Int, keyframe: Keyframe): this(newId, keyframe.frameId, keyframe.length) {
        keyframe.transformations.forEach {
            if (it is Reference) {
                val newReference = Reference(it)
                transformations.add(newReference)

                for (transformation in it.group.values) {
                    if (transformation.type == TransformationType.REFERENCE) {
                        continue
                    }
                    val newTransformation = Transformation(transformation)
                    newReference.group[transformation.type] = newTransformation
                    transformations.add(newTransformation)
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

    fun encode(keyframe: Keyframe): ByteArray {
        val out = ByteArrayOutputStream()
        val os = DataOutputStream(out)

        os.writeShort(1)
        os.writeByte(keyframe.transformations.size)

        // Write masks first
        for (transformation in keyframe.transformations) {
            os.writeByte(getMask(transformation))
        }

        // Write transformation values
        for (transformation in keyframe.transformations) {
            val mask = getMask(transformation)

            if (mask == 0) {
                continue
            }

            if (mask and 1 != 0) {
                writeTransformValue(os, transformation.offset.x)
            }

            if (mask and 2 != 0) {
                writeTransformValue(os, transformation.offset.y)
            }

            if (mask and 4 != 0) {
                writeTransformValue(os, transformation.offset.z)
            }
        }

        os.close()
        return out.toByteArray()
    }

    private fun writeTransformValue(os: DataOutputStream, value: Int) {
        if (value >= -64 && value < 64) {
            os.writeByte(value + 64)
        } else if (value >= -16384 && value < 16384) {
            os.writeShort(value + 49152)
        }
    }

    private fun getMask(transformation: Transformation): Int {
        val x = if (transformation.offset.x != 0) 1 else 0
        val y = if (transformation.offset.y != 0) 2 else 0
        val z = if (transformation.offset.z != 0) 4 else 0
        return x or y or z
    }
}