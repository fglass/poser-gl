package cache

import animation.Animation
import api.ICachePacker
import gui.component.Dialog
import gui.component.ProgressDialog
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.displee.CacheLibrary
import render.RenderContext

private val logger = KotlinLogging.logger {}

class PackManager(private val context: RenderContext, private val packer: ICachePacker) {

    fun pack() {
        val animation = context.animationHandler.currentAnimation ?: return
        if (animation.modified) {
            val dialog = ProgressDialog("Packing Animation", "Packing sequence ${animation.sequence.id}...", context)
            val listener = ProgressListener(dialog)
            dialog.display()
            packAnimation(animation, dialog, listener)
        } else {
            Dialog("Invalid Operation", "This animation has not been modified yet", context, 260f, 70f).display()
        }
    }

    private fun packAnimation(animation: Animation, dialog: ProgressDialog, listener: ProgressListener) {
        val library = CacheLibrary(context.cacheService.path)
        GlobalScope.launch {
            try {
                val archiveId = packer.packFrames(library, animation)
                packSequence(library, animation, archiveId, listener)
                updateIndices(library, listener) // After pack successful
                onFinish(animation, dialog)
            } catch (e: Exception) {
                logger.error(e) { "Exception encountered during packing" }
            } finally {
                library.close()
            }
        }
    }

    private fun packSequence(library: CacheLibrary, animation: Animation, archiveId: Int, listener: ProgressListener) {
        listener.change(0.0, "Packing sequence definition...")
        val sequence = animation.toSequence(archiveId)

        if (library.is317) {
            context.cacheService.animations.keys.max()?.let { packer.packSequence(library, sequence, it) }
        } else {
            packer.packSequence(library, sequence)
        }
    }

    private fun updateIndices(library: CacheLibrary, listener: ProgressListener) {
        library.getIndex(context.cacheService.loader.frameIndex).update(listener)
        library.getIndex(packer.sequenceConfigIndex).update(listener)
    }

    private fun onFinish(animation: Animation, dialog: ProgressDialog) {
        dialog.finish(animation.sequence.id)
        animation.modified = false
        context.gui.listPanel.animationList.updateElement(animation)
    }
}