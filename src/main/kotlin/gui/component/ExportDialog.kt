package gui.component

import org.liquidengine.legui.component.Button
import org.liquidengine.legui.component.RadioButton
import org.liquidengine.legui.component.RadioButtonGroup
import org.liquidengine.legui.component.optional.align.HorizontalAlign
import org.liquidengine.legui.event.MouseClickEvent
import transfer.ExportManager
import util.FileDialogs


class ExportDialog(manager: ExportManager, title: String, width: Float, height: Float):
                   Dialog(title, "", width, height) {

    init {
        this.remove(message)
        val group = RadioButtonGroup()
        val pack = RadioButton(".pack", 68f, 15f, 45f, 15f)
        val dat = RadioButton(".dat", 120f, 15f, 37f, 15f)

        val buttons = arrayOf(pack, dat)
        buttons.forEach {
            it.style.focusedStrokeColor = null
            it.textState.horizontalAlign = HorizontalAlign.RIGHT
            it.radioButtonGroup = group
            it.isChecked = it == pack
            container.add(it)
        }

        val export = Button("Export", 90f, 43f, 46f, 15f)
        export.style.focusedStrokeColor = null
        export.listenerMap.addListener(MouseClickEvent::class.java) { event ->
            if (event.action == MouseClickEvent.MouseClickAction.CLICK) {
                val name = FileDialogs.saveFile(if (pack.isChecked) "pack" else "dat", "")?: return@addListener
                if (pack.isChecked) manager.exportPack(name) else manager.exportDat(name)
            }
        }
        container.add(export)
    }
}