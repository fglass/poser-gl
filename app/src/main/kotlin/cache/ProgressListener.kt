package cache

import api.ProgressListenerWrapper
import gui.component.ProgressDialog

class ProgressListener(private val dialog: ProgressDialog): ProgressListenerWrapper() {

    override fun change(percentage: Double, message: String?) {
        dialog.update(percentage.toFloat(), message ?: "Packing...")
    }

    override fun finish(s1: String?, s2: String?) {
        // Unused
    }
}