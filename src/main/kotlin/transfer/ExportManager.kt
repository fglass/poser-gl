package transfer

import Processor
import gui.component.ExportDialog

class ExportManager(private val context: Processor) {

    fun export() { // TODO exit if no anim selected
        ExportDialog(this, "Export Animation", 230f, 92f).show(context.frame)
    }

    fun exportPack(file: String) {
        println("a")
    }

    fun exportDat(file: String) {
        println("b")
    }
}