package cache

import render.RenderContext
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
import com.google.inject.Guice
import java.util.HashSet
import net.runelite.cache.definitions.FrameDefinition
import net.runelite.cache.definitions.ItemDefinition
import net.runelite.cache.definitions.NpcDefinition


private val logger = KotlinLogging.logger {}

// TODO: plugins & thorough testing

class CacheService(private val context: RenderContext) {

    lateinit var loader: ICacheLoader
    var cachePath = ""
    var osrs = true
    var loaded = false

    var entities = HashMap<Int, NpcDefinition>()
    var items = HashMap<Int, ItemDefinition>()
    var animations = HashMap<Int, Animation>()
    var frames: HashMultimap<Int, FrameDefinition> = HashMultimap.create()
    var frameMaps = HashMap<Int, HashSet<Int>>()

    fun init(cachePath: String, pluginName: String) {
        this.cachePath = cachePath
        val injector = Guice.createInjector(LoadModule())
        val processor = injector.getInstance(PluginProcessor::class.java)
        loader = processor.getPlugin(pluginName)?: return

        try {
            val library = CacheLibrary(cachePath)
            load(library)
            library.close()
            osrs = library.isOSRS
            logger.info { "Loaded cache $cachePath with $pluginName plugin" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to load cache $cachePath with $pluginName plugin" }
        }
    }

    private fun load(library: CacheLibrary) {
        reset()
        entities = loader.loadNpcDefinitions(library)
        addPlayer()
        logger.info { "Loaded ${entities.size} entities" }

        items = loader.loadItemDefinitions(library)
        logger.info { "Loaded ${items.size} items" }

        frames = loader.loadFrameArchives(library)
        logger.info { "Loaded ${frames.keys().size} frames" }

        loadAnimations(library)
        logger.info { "Loaded ${animations.size} animations" }

        if (entities.size <= 1) {
            throw Exception("Cache loaded incorrectly")
        }
        loaded = true
    }

    private fun reset() {
        entities.clear()
        items.clear()
        animations.clear()
        frames.clear()
        frameMaps.clear()
    }

    private fun loadAnimations(library: CacheLibrary) {
        val sequences = loader.loadSequences(library)
        sequences.forEach {
            if (it.frameIDs != null) {
                animations[it.id] = Animation(context, it)
                addFrameMap(it)
            } else {
                logger.info { "Sequence ${it.id} contains no frames" }
            }
        }
    }

    fun addFrameMap(sequence: SequenceDefinition) {
        val archiveId = sequence.frameIDs.first() ushr 16
        val frames = frames[archiveId]
        
        if (frames.isNotEmpty()) {
            val frameMap = frames.first().framemap
            frameMaps.putIfAbsent(frameMap.id, HashSet())
            frameMaps[frameMap.id]?.add(sequence.id)
        }
    }

    private fun addPlayer() {
        val player = NpcDefinition(-1)
        player.name = "Player"
        player.walkAnimation = 819
        player.models = intArrayOf(230, 249, 292, 151, 176, 254, 181)
        entities[player.id] = player
    }

    fun loadModelDefinition(component: EntityComponent): ModelDefinition {
        val library = CacheLibrary(cachePath)
        val modelIndex = if (osrs) IndexType.MODEL.idOsrs else IndexType.MODEL.id317

        val model = library.getIndex(modelIndex).getArchive(component.id).getFile(0)
        val def = ModelLoader().load(component.id, model.data)

        if (component.originalColours != null && component.newColours != null) {
            for (i in component.originalColours.indices) {
                def.recolor(component.originalColours[i], component.newColours[i])
            }
        }
        library.close()
        return def
    }

    fun getMaxFrameArchive(library: CacheLibrary): Int {
        val frameIndex = if (osrs) IndexType.FRAME.idOsrs else IndexType.FRAME.id317
        return library.getIndex(frameIndex).lastArchive.id
    }

    fun pack() {
        val animation = context.animationHandler.currentAnimation?: return
        if (animation.modified) {
            val progress = ProgressDialog("Packing Animation", "Packing sequence ${animation.sequence.id}...",
                                          context, 230f, 92f)
            val listener = ProgressListener(progress)
            progress.display()

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
            Dialog("Invalid Operation", "This animation has not been modified yet", context, 260f, 70f).display()
        }
    }
}