package transfer

import render.RenderContext
import animation.Animation
import animation.Keyframe
import animation.ReferenceNode
import api.animation.IKeyframe
import api.animation.getMask
import api.definition.FrameMapDefinition
import gui.component.DatDialog
import gui.component.ExportDialog
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.File
import kotlin.reflect.KFunction2

class ExportManager(private val context: RenderContext) {

    private lateinit var dialog: ExportDialog
    private var lastExport = "" to ExportFormat.PGL

    fun openDialog() {
        context.animationHandler.currentAnimation?: return
        dialog = ExportDialog(context)
        dialog.display()
    }

    fun exportPgl(name: String) {
        val animation = context.animationHandler.currentAnimation ?: return
        val data = encodePgl(animation)
        File(name).writeBytes(data)
        lastExport = name to ExportFormat.PGL
        dialog.close()
    }

    private fun encodePgl(animation: Animation): ByteArray {
        val out = ByteArrayOutputStream()
        val os = DataOutputStream(out)

        os.writeByte(0) // Revision byte (0, 1) for backwards compatibility
        os.writeShort(animation.keyframes.size)

        for (keyframe in animation.keyframes) {
            os.writeShort(keyframe.length)
            encodeFrameMap(keyframe.frameMap, os)
            encodeKeyframe(keyframe, os)
        }

        // Other sequence attributes
        os.writeInt(animation.sequence.leftHandItem)
        os.writeInt(animation.sequence.rightHandItem)

        os.close()
        return out.toByteArray()
    }

    private fun encodeFrameMap(frameMap: FrameMapDefinition, stream: DataOutputStream) {
        stream.writeByte(frameMap.length)

        repeat(frameMap.length) {
            stream.writeByte(frameMap.types[it])
        }

        repeat(frameMap.length) {
            stream.writeByte(frameMap.maps[it].size)
        }

        repeat(frameMap.length) {
            repeat(frameMap.maps[it].size) { index ->
                stream.writeByte(frameMap.maps[it][index])
            }
        }
    }

    private fun encodeKeyframe(keyframe: Keyframe, stream: DataOutputStream) {
        stream.writeShort(keyframe.frameMap.id)
        val n = keyframe.transformations.filterIsInstance<ReferenceNode>().size
        stream.writeByte(n)

        for (transformation in keyframe.transformations) {

            if (transformation is ReferenceNode) {
                stream.writeShort(transformation.id)
                repeat(3) { i ->
                    stream.writeShort(transformation.delta[i])
                }
                stream.writeByte(transformation.children.size)

                transformation.children.forEach {
                    val child = it.value
                    stream.writeShort(child.id)
                    stream.writeByte(child.type.id)
                    repeat(3) { i ->
                        stream.writeShort(child.delta[i])
                    }
                }
            }
        }
    }

    fun exportDat(name: String) { // TODO: use plugin instead and don't tie to 317
        val animation = context.animationHandler.currentAnimation ?: return
        val data = encodeAnimation317(animation)
        File(name).writeBytes(data)
        lastExport = name to ExportFormat.DAT
        dialog.close()
        DatDialog(context, animation).display()
    }

    private fun encodeAnimation317(animation: Animation): ByteArray {
        val out = ByteArrayOutputStream()
        val os = DataOutputStream(out)

        val frameMap = animation.keyframes.first().frameMap
        os.write(encodeFrameMap317(frameMap))

        val modified = animation.keyframes.filter { it.modified }
        os.writeShort(modified.size)

        for ((i, keyframe) in modified.withIndex()) { // To decrement keyframe id
            os.write(encodeKeyframe317(keyframe, i))
        }

        os.close()
        return out.toByteArray()
    }

    private fun encodeFrameMap317(def: FrameMapDefinition): ByteArray {
        val out = ByteArrayOutputStream()
        val os = DataOutputStream(out)

        os.writeShort(def.length)

        for (i in 0 until def.length) {
            os.writeShort(def.types[i])
        }

        for (i in 0 until def.length) {
            os.writeShort(def.maps[i].size)
        }

        for (i in 0 until def.length) {
            for (j in def.maps[i]) {
                os.writeShort(j)
            }
        }

        os.close()
        return out.toByteArray()
    }

    private fun encodeKeyframe317(keyframe: IKeyframe, id: Int): ByteArray {
        val out = ByteArrayOutputStream()
        val os = DataOutputStream(out)

        os.writeShort(id)
        os.writeByte(keyframe.transformations.size)

        // Write transformation values
        var index = 0
        for (transformation in keyframe.transformations) {
            // Insert ignored masks to preserve transformation indices
            repeat(transformation.id - index) {
                os.writeByte(0)
            }
            index = transformation.id + 1

            val mask = getMask(transformation.delta)
            os.writeByte(mask)

            if (mask == 0) {
                continue
            }

            if (mask and 1 != 0) {
                os.writeShort(transformation.delta.x)
            }

            if (mask and 2 != 0) {
                os.writeShort(transformation.delta.y)
            }

            if (mask and 4 != 0) {
                os.writeShort(transformation.delta.z)
            }
        }
        os.close()
        return out.toByteArray()
    }

    fun redo() {
        val (name, format) = lastExport
        if (!name.isBlank()) {
            format.export.invoke(this, name)
        }
    }
}

enum class ExportFormat(val extension: String, val export: KFunction2<ExportManager, String, Unit>) {
    PGL("pgl", ExportManager::exportPgl),
    DAT("dat", ExportManager::exportDat)
}