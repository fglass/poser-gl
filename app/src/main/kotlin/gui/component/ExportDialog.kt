package gui.component

import render.RenderContext
import util.FileDialog
import org.liquidengine.legui.component.Button
import org.liquidengine.legui.component.RadioButton
import org.liquidengine.legui.component.RadioButtonGroup
import org.liquidengine.legui.component.optional.align.HorizontalAlign
import org.liquidengine.legui.event.MouseClickEvent
import transfer.ExportFormat

class ExportDialog(private val context: RenderContext): Dialog("Export Manager", "", context, 230f, 80f) {

    init {
        isDraggable = false
        container.remove(message)

        val group = RadioButtonGroup()
        val pgl = RadioButton(".${ExportFormat.PGL.extension}}", 68f, 9f, 37f, 15f)
        val dat = RadioButton(".${ExportFormat.DAT.extension}", 120f, 9f, 37f, 15f)

        val buttons = arrayOf(pgl, dat)
        buttons.forEach {
            it.style.focusedStrokeColor = null
            it.textState.horizontalAlign = HorizontalAlign.RIGHT
            it.radioButtonGroup = group
            it.isChecked = it == pgl
            container.add(it)
        }

        val export = Button("Export", 90f, 35f, 46f, 15f)
        export.style.focusedStrokeColor = null
        export.listenerMap.addListener(MouseClickEvent::class.java) { event ->
            if (event.action == MouseClickEvent.MouseClickAction.CLICK) {
                val format = if (pgl.isChecked) ExportFormat.PGL else ExportFormat.DAT
                val name = FileDialog.saveFile(format.extension)?: return@addListener
                format.export.invoke(context.exportManager, name)
            }
        }
        container.add(export)
    }
}