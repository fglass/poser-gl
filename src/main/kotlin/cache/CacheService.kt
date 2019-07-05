package cache

import CACHE_PATH
import Processor
import animation.Animation
import cache.load.CacheLoader
import cache.load.CacheLoader317
import cache.load.CacheLoaderOSRS
import cache.load.ModelLoader
import cache.pack.CachePacker317
import cache.pack.CachePackerOSRS
import com.google.common.collect.HashMultimap
import entity.EntityComponent
import gui.component.Popup
import gui.component.ProgressPopup
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.runelite.cache.definitions.*
import org.displee.CacheLibrary

private val logger = KotlinLogging.logger {}

class CacheService(private val context: Processor) {

    private val loader: CacheLoader
    private var osrs = false
    var loaded = false

    val entities = HashMap<Int, NpcDefinition>()
    val items = HashMap<Int, ItemDefinition>()
    var animations = HashMap<Int, Animation>()
    val frames: HashMultimap<Int, FrameDefinition> = HashMultimap.create()

    init {
        val library = CacheLibrary(CACHE_PATH)
        osrs = library.isOSRS
        loader = if (osrs) CacheLoaderOSRS(context, this) else CacheLoader317(context, this)

        try {
            val revision = if (osrs) "OSRS" else "317"
            logger.info { "Loaded $revision cache" }

            addPlayer()
            loader.loadNpcDefintions(library)
            logger.info { "Loaded ${entities.size} entities" }

            loader.loadItemDefinitions(library)
            logger.info { "Loaded ${items.size} items" }

            loader.loadSequences(library)
            logger.info { "Loaded ${animations.size} sequences" }
            loaded = true
        } catch (e: Exception) {
            logger.error(e) { "Failed to load cache" }
        } finally {
            library.close()
        }
    }

    private fun addPlayer() {
        val player = NpcDefinition(-1)
        player.name = "Player"
        player.models = intArrayOf(230, 249, 292, 151, 176, 254, 181)
        entities[player.id] = player
    }

    fun loadModelDefinition(component: EntityComponent): ModelDefinition {
        val library = CacheLibrary(CACHE_PATH)
        val modelIndex = if (osrs) IndexType.MODEL.idOsrs else IndexType.MODEL.id317

        val model = library.getIndex(modelIndex).getArchive(component.id).getFile(0)
        val def = ModelLoader().load(component.id, model.data)

        if (component.originalColours != null && component.newColours != null) {
            for (i in 0 until component.originalColours.size) {
                def.recolor(component.originalColours[i], component.newColours[i])
            }
        }
        library.close()
        return def
    }

    fun getFrameArchive(archiveId: Int): MutableSet<FrameDefinition> {
        if (archiveId !in frames.keySet()) {
            loader.loadFrameArchive(archiveId)
        }
        return frames.get(archiveId)
    }

    fun getMaxFrameArchive(library: CacheLibrary): Int {
        val frameIndex = if (osrs) IndexType.FRAME.idOsrs else IndexType.FRAME.id317
        return library.getIndex(frameIndex).lastArchive.id
    }

    fun pack(animation: Animation) {
        if (animation.modified) {
            val progress = ProgressPopup("Packing Animation", "Packing sequence ${animation.sequence.id}...", 230f, 92f)
            val listener = ProgressListener(progress)
            progress.show(context.frame)

            // Asynchronously pack animation
            val packer = if (osrs) CachePackerOSRS(this) else CachePacker317(this)
            GlobalScope.launch {
                try {
                    packer.packAnimation(animation, listener)
                    progress.finish(animation.sequence.id)
                    animation.saved = true
                    context.gui.listPanel.animationList.updateElement(animation)
                } catch (e: Exception) {
                    logger.error(e) { "Pack exception encountered" }
                }
            }
        } else {
            Popup("Invalid Operation", "This animation has not been modified yet", 260f, 70f).show(context.frame)
        }
    }
}