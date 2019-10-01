package cache

import render.RenderContext
import animation.Animation
import com.google.common.collect.HashMultimap
import entity.EntityComponent
import gui.component.Dialog
import gui.component.ProgressDialog
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import api.ICacheLoader
import api.ICachePacker
import mu.KotlinLogging
import net.runelite.cache.definitions.*
import org.displee.CacheLibrary
import java.util.HashSet
import net.runelite.cache.definitions.FrameDefinition
import net.runelite.cache.definitions.ItemDefinition
import net.runelite.cache.definitions.NpcDefinition

private val logger = KotlinLogging.logger {}

class CacheService(private val context: RenderContext) {

    var path = ""
    var osrs = true
    var loaded = false
    lateinit var loader: ICacheLoader
    lateinit var packer: ICachePacker

    var entities = HashMap<Int, NpcDefinition>()
    var items = HashMap<Int, ItemDefinition>()
    var animations = HashMap<Int, Animation>()
    var frames: HashMultimap<Int, FrameDefinition> = HashMultimap.create()
    var frameMaps = HashMap<Int, HashSet<Int>>()

    fun init(path: String, loader: ICacheLoader) {
        this.path = path
        this.loader = loader
        packer = context.packers.first { it.toString() == loader.toString() }

        try {
            val library = CacheLibrary(path)
            osrs = library.isOSRS
            load(library)
            addPlayer()
            library.close()
            logger.info { "Loaded cache $path with $loader plugin" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to load cache $path with $loader plugin" }
        }
    }

    private fun load(library: CacheLibrary) {
        entities = loader.loadNpcDefinitions(library)
        logger.info { "Loaded ${entities.size} npcs" }

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

    private fun loadAnimations(library: CacheLibrary) {
        animations.clear()
        frameMaps.clear()
        val sequences = loader.loadSequences(library)
        sequences.forEach {
            when {
                it.frameIDs != null -> {
                    val animation = Animation(context, it)
                    animations[it.id] = animation
                    addFrameMap(animation)
                }
                else -> logger.error { "Sequence ${it.id} contains no frames" }
            }
        }
    }

    fun addFrameMap(animation: Animation) {
        val frameMap = when {
            animation.keyframes.isNotEmpty() -> animation.getFrameMap()
            else -> {
                val archiveId = animation.sequence.frameIDs.first() ushr 16
                val frames = frames[archiveId]
                frames.firstOrNull()?.framemap?: return
            }
        }
        frameMaps.putIfAbsent(frameMap.id, HashSet())
        frameMaps[frameMap.id]?.add(animation.sequence.id)
    }

    private fun addPlayer() {
        val player = NpcDefinition(-1)
        player.name = "Player"
        player.walkAnimation = 819
        player.models = intArrayOf(230, 249, 292, 151, 176, 254, 181)
        entities[player.id] = player
    }

    fun loadModelDefinition(component: EntityComponent): ModelDefinition { // TODO: move to plugins
        val library = CacheLibrary(path)
        val modelIndex = if (osrs) 7 else 1

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

    fun pack() { // TODO: packing error on reload
        val animation = context.animationHandler.currentAnimation?: return
        if (animation.modified) {
            val dialog = ProgressDialog("Packing Animation", "Packing sequence ${animation.sequence.id}...", context)
            val listener = ProgressListener(dialog)
            dialog.display()
            invokePacker(animation, listener, dialog)
        } else {
            Dialog("Invalid Operation", "This animation has not been modified yet", context, 260f, 70f).display()
        }
    }

    private fun invokePacker(animation: Animation, listener: ProgressListener, dialog: ProgressDialog) {
        val library = CacheLibrary(path)
        GlobalScope.launch { // Asynchronously pack
            try {
                val archiveId = getMaxFrameArchive(library) + 1
                val maxAnimationId = animations.keys.max()?: throw Exception()  // Only used in 317 plugins
                packer.packAnimation(animation, archiveId, library, listener, maxAnimationId)

                dialog.finish(animation.sequence.id)
                animation.modified = false
                context.gui.listPanel.animationList.updateElement(animation)
            } catch (e: Exception) {
                logger.error(e) { "Exception encountered during packing" }
            } finally {
                library.close()
            }
        }
    }

    fun getMaxFrameArchive(library: CacheLibrary): Int { // TODO: move to plugins
        val frameIndex = if (osrs) 0 else 2
        return library.getIndex(frameIndex).lastArchive.id
    }
}