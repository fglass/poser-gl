package transfer

import render.RenderContext
import animation.Animation
import animation.Keyframe
import animation.ReferenceNode
import cache.pack.CachePacker317
import gui.component.DatDialog
import gui.component.ExportDialog
import net.runelite.cache.definitions.FramemapDefinition
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.File

class ExportManager(private val context: RenderContext) {

    private lateinit var dialog: ExportDialog

    fun openDialog() {
        context.animationHandler.currentAnimation?: return
        dialog = ExportDialog(context)
        dialog.display()
    }

    fun exportPgl(name: String) {
        val animation = context.animationHandler.currentAnimation ?: return
        val data = encodePgl(animation)
        File(name).writeBytes(data)
        dialog.close()
    }

    private fun encodePgl(animation: Animation): ByteArray {
        val out = ByteArrayOutputStream()
        val os = DataOutputStream(out)

        os.writeByte(if (context.cacheService.osrs) 1 else 0) // Revision byte
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

    private fun encodeFrameMap(frameMap: FramemapDefinition, stream: DataOutputStream) {
        stream.writeByte(frameMap.length)

        repeat(frameMap.length) {
            stream.writeByte(frameMap.types[it])
        }

        repeat(frameMap.length) {
            stream.writeByte(frameMap.frameMaps[it].size)
        }

        repeat(frameMap.length) {
            repeat(frameMap.frameMaps[it].size) { index ->
                stream.writeByte(frameMap.frameMaps[it][index])
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

    fun exportDat(name: String) {
        val animation = context.animationHandler.currentAnimation ?: return
        val data = CachePacker317(context.cacheService).encodeAnimation(animation)
        File(name).writeBytes(data)
        dialog.close()
        DatDialog(context, animation).display()
    }
}