package cache

import gui.component.ProgressPopup
import org.displee.progress.AbstractProgressListener

class ProgressListener(private val popup: ProgressPopup): AbstractProgressListener() {

    override fun change(percentage: Double, message: String?) {
        popup.update(percentage.toFloat(), message?: "Packing...")
    }

    override fun finish(s1: String?, s2: String?) {
        // Unused
    }
}