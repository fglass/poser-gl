package transfer

import Processor
import animation.Animation
import gui.component.ExportDialog
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

    fun exportPack(name: String) {
        val animation = context.animationHandler.currentAnimation?: return
        val pack = encodePack(animation)

        File(name).writeBytes(pack)
        dialog.close()
    }

    fun exportDat(name: String) {
        // TODO
    }

    private fun encodePack(animation: Animation): ByteArray {
        val out = ByteArrayOutputStream()
        val os = DataOutputStream(out)

        /* Sequence */
        os.writeByte(1)
        os.writeShort(animation.keyframes.size)

        /*for (frameId in sequence.frameIDs) { // Need?
            os.writeInt(frameId)
        }*/
        animation.keyframes.forEach {
            os.writeShort(it.length)
        }
        os.writeByte(0)

        /* Frames */
        var modified = 0
        animation.keyframes.forEach {
            if (it.modified) {
                os.write(it.encode(modified++, true))
            }
        }

        os.close()
        return out.toByteArray()
    }
}