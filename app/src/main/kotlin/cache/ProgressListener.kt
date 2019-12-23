package cache

import gui.component.ProgressDialog
import org.displee.progress.AbstractProgressListener

class ProgressListener(private val dialog: ProgressDialog) : AbstractProgressListener() {

    override fun change(percentage: Double, message: String?) {
        dialog.update(percentage.toFloat(), message ?: "Packing...")
    }

    override fun finish(s1: String?, s2: String?) {
        // Unused
    }
}