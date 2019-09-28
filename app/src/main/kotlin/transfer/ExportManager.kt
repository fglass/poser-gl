package transfer

import render.RenderContext
import animation.Animation
import animation.Keyframe
import animation.ReferenceNode
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
        val data = encodeAnimation317(animation)
        File(name).writeBytes(data)
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
            os.write(keyframe.encode(i, false))
        }

        os.close()
        return out.toByteArray()
    }

    private fun encodeFrameMap317(def: FramemapDefinition): ByteArray {
        val out = ByteArrayOutputStream()
        val os = DataOutputStream(out)

        os.writeShort(def.length)

        for (i in 0 until def.length) {
            os.writeShort(def.types[i])
        }

        for (i in 0 until def.length) {
            os.writeShort(def.frameMaps[i].size)
        }

        for (i in 0 until def.length) {
            for (j in def.frameMaps[i]) {
                os.writeShort(j)
            }
        }

        os.close()
        return out.toByteArray()
    }
}