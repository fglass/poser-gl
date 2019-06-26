package animation

import CACHE_PATH
import Processor
import com.google.common.collect.HashMultimap
import net.runelite.cache.ConfigType
import net.runelite.cache.IndexType
import net.runelite.cache.definitions.FrameDefinition
import net.runelite.cache.definitions.ModelDefinition.*
import net.runelite.cache.definitions.SequenceDefinition
import net.runelite.cache.definitions.loaders.FrameLoader
import net.runelite.cache.definitions.loaders.FramemapLoader
import net.runelite.cache.definitions.loaders.SequenceLoader
import net.runelite.cache.fs.Store
import shader.ShadingType
import java.io.File

// https://www.rune-server.ee/runescape-development/rs2-client/tutorials/340745-runescapes-rendering-animation-system.html

const val MAX_LENGTH = 999

class AnimationHandler(private val context: Processor) {

    val sequences = java.util.HashMap<Int, SequenceDefinition>()
    val frames: HashMultimap<Int, FrameDefinition> = HashMultimap.create()

    private var currentAnimation: Animation? = null
    private var frameCount = 0
    private var frameLength = 0

    var playing = false
    private var timer = 0

    init {
        val store = Store(File(CACHE_PATH))
        store.load()
        loadSequences(store)
        loadFrames(store)
        store.close()
    }

    private fun loadSequences(store: Store) {
        val storage = store.storage
        val index = store.getIndex(IndexType.CONFIGS)
        val archive = index.getArchive(ConfigType.SEQUENCE.id)

        val archiveData = storage.loadArchive(archive)
        val files = archive.getFiles(archiveData)

        for (file in files.files) {
            val loader = SequenceLoader()
            val seq = loader.load(file.fileId, file.contents)
            sequences[file.fileId] = seq
        }
        println("Loaded ${sequences.size} sequences")
    }

    private fun loadFrames(store: Store) {
        val storage = store.storage
        val frameIndex = store.getIndex(IndexType.FRAMES)
        val frameMapIndex = store.getIndex(IndexType.FRAMEMAPS)

        for (archive in frameIndex.archives) {
            var archiveData = storage.loadArchive(archive)
            val archiveFiles = archive.getFiles(archiveData)
            for (archiveFile in archiveFiles.files) {
                val contents = archiveFile.contents

                val frameMapArchiveId = (contents[0].toInt() and 0xff) shl 8 or (contents[1].toInt() and 0xff)

                val frameMapArchive = frameMapIndex.archives[frameMapArchiveId]
                archiveData = storage.loadArchive(frameMapArchive)
                val frameMapContents = frameMapArchive.decompress(archiveData)

                val frameMap = FramemapLoader().load(frameMapArchive.archiveId, frameMapContents)
                val frame = FrameLoader().load(frameMap, archiveFile.fileId, contents)

                frames.put(archive.archiveId, frame)
            }
        }
        println("Loaded ${frames.size()} frames")
    }

    fun play(sequence: SequenceDefinition) {
        currentAnimation = Animation(sequence, this)
        context.gui.animationPanel.play(sequence)
        reset()
        playing = true
    }

    private fun reset() {
        setFrame(0, 0, 0)
    }

    fun setFrame(time: Int, frame: Int, offset: Int) {
        timer = time
        frameCount = frame
        frameLength = currentAnimation!!.keyframes[frame].length - offset
    }

    fun tick() {
        if (currentAnimation == null || context.entity == null) {
            return
        }

        if (timer > MAX_LENGTH) {
            reset()
        }

        if (playing) {
            // Adjust timer
            if (getFrameIndex() == 0 && frameLength <= 0) { // Animation restarted
                timer = 0
            } else {
                timer++
            }

            // Traverse frame
            if (frameLength-- <= 0) {
                frameCount++
                frameLength = currentAnimation!!.keyframes[getFrameIndex()].length
            }
        }

        val keyframe = currentAnimation!!.keyframes[getFrameIndex()]
        applyKeyframe(keyframe)
        context.gui.animationPanel.tickCursor(timer)
    }

    private fun getFrameIndex(): Int {
        return frameCount % currentAnimation!!.keyframes.size
    }

    private fun applyKeyframe(keyframe: Keyframe) {
        // Reset from last frame
        context.framebuffer.nodeRenderer.reset()
        animOffsetX = 0
        animOffsetY = 0
        animOffsetZ = 0

        val entity = context.entity ?: return
        val def = entity.model.definition
        def.resetAnim()

        for (transformation in keyframe.transformations) {
            if (transformation is Reference) {
                context.framebuffer.nodeRenderer.addNode(def, transformation)
            }
            transformation.apply(def)
        }

        // Load transformed model
        context.loader.cleanUp()
        entity.model = context.datLoader.parse(def, context.framebuffer.shadingType == ShadingType.FLAT)
    }

    fun transformNode(type: TransformationType, coordIndex: Int, newValue: Int) {
        val selected = context.framebuffer.nodeRenderer.selected ?: return
        var id = selected.reference.id

        if (type != TransformationType.REFERENCE) {
            id = selected.reference.children[type.id - 1].id // TODO
        }

        val keyframe = currentAnimation!!.keyframes[getFrameIndex()]
        val transformation = keyframe.transformations.first { it.id == id }

        when (coordIndex) { // TODO
            0 -> transformation.offset.x = newValue
            1 -> transformation.offset.y = newValue
            2 -> transformation.offset.z = newValue
        }
    }

    fun resetAnimation() {
        currentAnimation = null
        frameCount = 0
        frameLength = 0
        context.framebuffer.nodeRenderer.reset()
        context.gui.animationPanel.stop()
    }
}