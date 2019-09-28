package animation

import api.IKeyframe
import render.RenderContext
import net.runelite.cache.definitions.FramemapDefinition
import net.runelite.cache.definitions.ModelDefinition.*
import shader.ShadingType
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

class Keyframe(val id: Int = -1, val frameId: Int = -1, var length: Int = -1,
               val frameMap: FramemapDefinition = FramemapDefinition()): IKeyframe {

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

    var modified = false
    val transformations = ArrayList<Transformation>()

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

    override fun encode(id: Int, osrs: Boolean): ByteArray {
        val out = ByteArrayOutputStream()
        val os = DataOutputStream(out)

        os.writeShort(if (osrs) frameMap.id else id)
        os.writeByte(transformations.size)

        // Write masks first if OSRS
        if (osrs) {
            for (transformation in transformations) {
                os.writeByte(getMask(transformation))
            }
        }

        // Write transformation values
        for (transformation in transformations) {
            val mask = getMask(transformation)
            if (!osrs) {
                os.writeByte(mask)
            }

            if (mask == 0) {
                continue
            }

            if (mask and 1 != 0) {
                writeSmartShort(os, osrs, transformation.delta.x)
            }

            if (mask and 2 != 0) {
                writeSmartShort(os, osrs, transformation.delta.y)
            }

            if (mask and 4 != 0) {
                writeSmartShort(os, osrs, transformation.delta.z)
            }
        }
        os.close()
        return out.toByteArray()
    }

    private fun writeSmartShort(os: DataOutputStream, osrs: Boolean, value: Int) {
        if (!osrs) {
            os.writeShort(value)
            return
        }
        if (value >= -64 && value < 64) {
            os.writeByte(value + 64)
        } else if (value >= -16384 && value < 16384) {
            os.writeShort(value + 49152)
        }
    }

    private fun getMask(transformation: Transformation): Int {
        val x = if (transformation.delta.x != 0) 1 else 0
        val y = if (transformation.delta.y != 0) 2 else 0
        val z = if (transformation.delta.z != 0) 4 else 0
        return x or y or z
    }

    override fun isModified(): Boolean {
        return modified
    }

    override fun getFrameMapDef(): FramemapDefinition {
        return frameMap
    }
}