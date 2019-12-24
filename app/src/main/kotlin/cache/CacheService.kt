package cache

import animation.Animation
import api.cache.ICacheLoader
import api.definition.*
import com.google.common.collect.HashMultimap
import entity.EntityComponent
import entity.HIGHER_REV_SCALE
import mu.KotlinLogging
import org.displee.CacheLibrary
import render.RenderContext

private val logger = KotlinLogging.logger {}
var isHigherRev = false

class CacheService(private val context: RenderContext) {

    var path = ""
    var loaded = false
    lateinit var loader: ICacheLoader
    lateinit var packManager: PackManager

    var entities = HashMap<Int, NpcDefinition>()
    var items = HashMap<Int, ItemDefinition>()
    var animations = HashMap<Int, Animation>()
    private var frames: HashMultimap<Int, FrameDefinition> = HashMultimap.create()

    fun init(path: String, loader: ICacheLoader) {
        this.path = path
        this.loader = loader
        packManager = PackManager(context, context.packers.first { it.toString() == loader.toString() }) // TODO: couple better

        try {
            val library = CacheLibrary(path)
            load(library)
            library.close()
            logger.info { "Loaded cache $path with $loader plugin" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to load cache $path with $loader plugin" }
        }
    }

    private fun load(library: CacheLibrary) {
        isHigherRev = !library.is317 && !library.isOSRS
        context.entityHandler.scale = if (isHigherRev) 1 / HIGHER_REV_SCALE else 1f // Downscale for higher revs

        entities = loader.loadNpcDefs(library)
        logger.info { "Loaded ${entities.size} npcs" }
        addPlayer()

        items = loader.loadItemDefs(library)
        logger.info { "Loaded ${items.size} items" }

        val sequences = loader.loadSequences(library)
        animations = HashMap(sequences.associateBy(SequenceDefinition::id) { Animation(context, it) })
        logger.info { "Loaded ${animations.size} animations" }

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

    fun loadModelDef(component: EntityComponent): ModelDefinition {
        val library = CacheLibrary(path)
        val def = loader.loadModelDef(library, component.id)
        library.close()

        if (component.originalColours != null && component.newColours != null) {
            for (i in component.originalColours.indices) {
                def.recolour(component.originalColours[i], component.newColours[i])
            }
        }
        return def
    }

    private fun ModelDefinition.recolour(old: Short, new: Short) {
        repeat(faceCount) {
            if (faceColors[it] == old) {
                faceColors[it] = new
            }
        }
    }

    fun getFrameArchive(archiveId: Int): Set<FrameDefinition> {
        val archive = frames.get(archiveId)
        if (archive.isNotEmpty()) {
            return archive
        }

        val library = CacheLibrary(path)
        val loaded = loader.loadFrameArchive(library, archiveId)
        library.close()

        frames.putAll(archiveId, loaded) // Cache
        return loaded
    }
}