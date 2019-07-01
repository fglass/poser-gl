package animation

import Processor
import net.runelite.cache.definitions.ModelDefinition.*
import net.runelite.cache.io.InputStream
import shader.ShadingType
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

class Keyframe(val id: Int, val frameId: Int, var length: Int) {

    var modified = true // TODO

    // Copy constructor
    constructor(newId: Int, keyframe: Keyframe): this(newId, keyframe.frameId, keyframe.length) {
        keyframe.transformations.forEach {
            if (it is Reference) {
                val newReference = Reference(it)
                transformations.add(newReference)

                for (transformation in it.children.values) {
                    val newTransformation = Transformation(transformation)
                    newReference.children[transformation.type] = newTransformation
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
        context.nodeRenderer.nodes.clear()
        animOffsetX = 0
        animOffsetY = 0
        animOffsetZ = 0

        val entity = context.entity?: return
        val def = entity.model.definition
        def.resetAnim()

        for (transformation in transformations) {
            if (transformation is Reference) {
                context.nodeRenderer.addNode(def, transformation)
            }
            transformation.apply(def)
        }

        // Load transformed model
        context.loader.cleanUp()
        entity.model = context.datLoader.parse(def, context.framebuffer.shadingType == ShadingType.FLAT)
    }

    //val test = FrameEncoder().encode(keyframe)
    //val test2 = FrameLoader().load(frame.framemap, keyframe.id, test)
    fun encode(): ByteArray {
        val out = ByteArrayOutputStream()
        val os = DataOutputStream(out)

        val frameMapId = transformations.first().frameMapId
        // (contents[0].toInt() and 0xff) shl 8 or (contents[1].toInt() and 0xff)
        os.writeByte(frameMapId ushr 8)
        os.writeByte(frameMapId and 0xFF)

        //os.writeShort(1)
        os.writeByte(transformations.size)

        // Write masks first
        for (transformation in transformations) {
            os.writeByte(getMask(transformation))
        }

        // Write transformation values
        for (transformation in transformations) {
            val mask = getMask(transformation)

            if (mask == 0) {
                continue
            }

            if (mask and 1 != 0) {
                writeSmartShort(os, transformation.offset.x)
            }

            if (mask and 2 != 0) {
                writeSmartShort(os, transformation.offset.y)
            }

            if (mask and 4 != 0) {
                writeSmartShort(os, transformation.offset.z)
            }
        }
        os.close()
        return out.toByteArray()
    }

    private fun writeSmartShort(os: DataOutputStream, value: Int) {
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