package transfer

import Processor
import animation.Animation
import animation.Keyframe
import animation.ReferenceNode
import gui.component.ExportDialog
import net.runelite.cache.definitions.FramemapDefinition
import org.liquidengine.legui.component.Dialog
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.File

class ExportManager(private val context: Processor) {

    private lateinit var dialog: Dialog

    fun openDialog() {
        context.animationHandler.currentAnimation?: return
        dialog = ExportDialog(this, "Export Animation", 230f, 92f)
        dialog.show(context.frame)
    }

    fun exportPgl(name: String) {
        val animation = context.animationHandler.currentAnimation ?: return
        val pack = encodePgl(animation)

        File(name).writeBytes(pack)
        dialog.close()
    }

    private fun encodePgl(animation: Animation): ByteArray {
        val out = ByteArrayOutputStream()
        val os = DataOutputStream(out)

        os.writeByte(if (context.cacheService.osrs) 1 else 0) // Revision byte
        os.writeShort(animation.keyframes.size)

        for (keyframe in animation.keyframes) {
            os.writeShort(keyframe.length)
            keyframe.frameMap.encode(os)
            keyframe.encode(os)
        }

        os.close()
        return out.toByteArray()
    }

    private fun FramemapDefinition.encode(stream: DataOutputStream) {
        stream.writeByte(length)

        for (i in 0 until length) {
            stream.writeByte(types[i])
        }

        for (i in 0 until length) {
            stream.writeByte(frameMaps[i].size)
        }

        for (i in 0 until length) {
            for (j in 0 until frameMaps[i].size) {
                stream.writeByte(frameMaps[i][j])
            }
        }
    }

    private fun Keyframe.encode(stream: DataOutputStream) {
        stream.writeShort(frameMap.id)

        val n = transformations.filterIsInstance<ReferenceNode>().size
        stream.writeByte(n)

        for (transformation in transformations) {

            if (transformation is ReferenceNode) {
                stream.writeShort(transformation.id)
                stream.writeShort(transformation.delta.x)
                stream.writeShort(transformation.delta.y)
                stream.writeShort(transformation.delta.z)
                stream.writeByte(transformation.children.size)

                transformation.children.forEach {
                    val child = it.value
                    stream.writeShort(child.id)
                    stream.writeByte(child.type.id)

                    stream.writeShort(child.delta.x)
                    stream.writeShort(child.delta.y)
                    stream.writeShort(child.delta.z)
                }
            }
        }
    }

    fun exportDat(name: String) {
        // TODO
    }
}