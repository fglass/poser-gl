package gui.component

import render.RenderContext
import org.liquidengine.legui.component.ProgressBar

class ProgressDialog(
    title: String,
    message: String,
    context: RenderContext,
    width: Float = 230f,
    height: Float = 92f
) : Dialog(title, message, context, width, height) {

    private val progressBar = ProgressBar(size.x / 2 - 50, 43f, 100f, 10f)

    init { // TODO: add border
        isCloseable = false
        progressBar.value = 0f
        container.add(progressBar)
    }

    fun update(value: Float, text: String) {
        progressBar.value = value
        message.textState.text = text
    }

    fun finish(id: Int) {
        update(100f, "Successfully packed animation $id")
        titleTextState.text = ""
        isCloseable = true
    }
}