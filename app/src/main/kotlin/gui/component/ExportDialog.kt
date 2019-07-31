package gui.component

import org.liquidengine.legui.component.Button
import org.liquidengine.legui.component.RadioButton
import org.liquidengine.legui.component.RadioButtonGroup
import org.liquidengine.legui.component.optional.align.HorizontalAlign
import org.liquidengine.legui.event.MouseClickEvent
import transfer.ExportManager
import util.FileDialogs

class ExportDialog(manager: ExportManager): Dialog("Export Manager", "", 230f, 92f) {

    init {
        isDraggable = false
        container.remove(message)

        val group = RadioButtonGroup()
        val pgl = RadioButton(".pgl", 68f, 15f, 37f, 15f)
        val dat = RadioButton(".dat", 120f, 15f, 37f, 15f)

        val buttons = arrayOf(pgl, dat)
        buttons.forEach {
            it.style.focusedStrokeColor = null
            it.textState.horizontalAlign = HorizontalAlign.RIGHT
            it.radioButtonGroup = group
            it.isChecked = it == pgl
            container.add(it)
        }

        val export = Button("Export", 90f, 43f, 46f, 15f)
        export.style.focusedStrokeColor = null
        export.listenerMap.addListener(MouseClickEvent::class.java) { event ->
            if (event.action == MouseClickEvent.MouseClickAction.CLICK) {
                val name = FileDialogs.saveFile(if (pgl.isChecked) "pgl" else "dat", ".")?: return@addListener
                if (pgl.isChecked) manager.exportPgl(name) else manager.exportDat(name)
            }
        }
        container.add(export)
    }
}