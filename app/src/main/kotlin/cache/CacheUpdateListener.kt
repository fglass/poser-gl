package cache

import api.cache.ProgressListener
import gui.component.ProgressDialog
import com.displee.cache.ProgressListener as DispleeProgressListener

class CacheUpdateListener(private val dialog: ProgressDialog) : ProgressListener, DispleeProgressListener {
    override fun notify(progress: Double, message: String?) {
        dialog.update(progress.toFloat(), message ?: "Packing...")
    }
}