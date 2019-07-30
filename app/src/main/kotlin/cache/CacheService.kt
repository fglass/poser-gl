package cache

import Processor
import animation.Animation
import cache.load.*
import cache.pack.CachePacker317
import cache.pack.CachePackerOSRS
import com.google.common.collect.HashMultimap
import entity.EntityComponent
import gui.component.Dialog
import gui.component.ProgressDialog
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.runelite.cache.definitions.*
import org.displee.CacheLibrary

private val logger = KotlinLogging.logger {}

class CacheService(private val context: Processor) {

    lateinit var loader: CacheLoader
    var cachePath = ""
    var osrs = true
    var loaded = false

    val entities = HashMap<Int, NpcDefinition>()
    val items = HashMap<Int, ItemDefinition>()
    var animations = HashMap<Int, Animation>()
    val frames: HashMultimap<Int, FrameDefinition> = HashMultimap.create()

    fun init(cachePath: String, pluginName: String) {
        this.cachePath = cachePath
        loader = when (pluginName) {
            "OSRS" -> CacheLoaderOSRS(context, this)
            "317" -> AltCacheLoader317(context, this)
            else -> CacheLoader317(context, this)
        }

        try {
            val library = CacheLibrary(cachePath)
            osrs = library.isOSRS
            load(library)
            library.close()
            logger.info { "Loaded cache $cachePath with $pluginName plugin" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to load cache $cachePath with $pluginName plugin" }
        }
    }

    private fun load(library: CacheLibrary) {
        addPlayer()
        loader.loadNpcDefintions(library)
        logger.info { "Loaded ${entities.size} entities" }

        loader.loadItemDefinitions(library)
        logger.info { "Loaded ${items.size} items" }

        loader.loadSequences(library)
        logger.info { "Loaded ${animations.size} sequences" }

        if (entities.size <= 1) {
            throw Exception("Cache loaded incorrectly")
        }
        loaded = true
    }

    private fun addPlayer() {
        val player = NpcDefinition(-1)
        player.name = "Player"
        player.models = intArrayOf(230, 249, 292, 151, 176, 254, 181)
        entities[player.id] = player
    }

    fun loadModelDefinition(component: EntityComponent): ModelDefinition {
        val library = CacheLibrary(cachePath)
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

    fun pack() {
        val animation = context.animationHandler.currentAnimation?: return
        if (animation.modified) {
            val progress = ProgressDialog("Packing Animation", "Packing sequence ${animation.sequence.id}...", 230f, 92f)
            val listener = ProgressListener(progress)
            progress.show(context.frame)

            // Asynchronously pack animation
            val packer = if (osrs) CachePackerOSRS(this) else CachePacker317(this)
            GlobalScope.launch {
                try {
                    packer.packAnimation(animation, listener)
                    progress.finish(animation.sequence.id)
                    animation.modified = false
                    context.gui.listPanel.animationList.updateElement(animation)
                } catch (e: Exception) {
                    logger.error(e) { "Pack exception encountered" }
                }
            }
        } else {
            Dialog("Invalid Operation", "This animation has not been modified yet", 260f, 70f).show(context.frame)
        }
    }
}