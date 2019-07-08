package gui.component

import org.liquidengine.legui.component.ProgressBar

class ProgressDialog(title: String, message: String, width: Float, height: Float): Dialog(title, message, width, height) {

    private val progressBar = ProgressBar(size.x / 2 - 50, 43f, 100f, 10f)

    init {
        isCloseable = false
        progressBar.value = 0f
        container.add(progressBar)
    }

    fun update(value: Float, text: String) {
        progressBar.value = value
        message.textState.text = text
    }

    fun finish(id: Int) {
        update(100f, "Successfully packed sequence $id")
        titleTextState.text = ""
        isCloseable = true
    }
}