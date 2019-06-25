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

    private var currentSequence: SequenceDefinition = SequenceDefinition(-1)
    val sequences = java.util.HashMap<Int, SequenceDefinition>()
    private val frames = HashMultimap.create<Int, FrameDefinition>()
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
        playing = true
        currentSequence = sequence
        reset()
        context.gui.animationPanel.play(sequence)
    }

    private fun reset() {
        setFrame(0, 0, 0)
    }

    fun setFrame(time: Int, frame: Int, offset: Int) {
        timer = time
        frameCount = frame
        frameLength = currentSequence.frameLenghts[frame] - offset
    }

    fun tick() {
        if (currentSequence.id == -1 || context.entity == null) {
            return
        }

        if (timer > MAX_LENGTH) {
            reset()
        }

        if (playing) {
            if (getFrameIndex() == 0 && frameLength <= 0) { // Animation restarted
                timer = 0
            } else {
                timer++
            }

            if (frameLength-- <= 0) { // Traverse frame
                frameCount++
                frameLength = currentSequence.frameLenghts[getFrameIndex()]
            }
        }

        val seqFrameId = currentSequence.frameIDs[getFrameIndex()]
        val frames = frames.get(seqFrameId.ushr(16))
        val frameFileId = seqFrameId and 0xFFFF
        val frame = frames.stream().filter { frame -> frame.id == frameFileId }.findFirst().get()

        applyFrame(frame)
        context.gui.animationPanel.tickCursor(timer)
    }

    private fun getFrameIndex(): Int {
        return frameCount % currentSequence.frameIDs.size
    }

    private fun applyFrame(frame: FrameDefinition) {
        // Reset from last frame
        context.framebuffer.jointRenderer.reset()
        animOffsetX = 0
        animOffsetY = 0
        animOffsetZ = 0

        val entity = context.entity ?: return
        val frameMap = frame.framemap
        val def = entity.model.definition
        def.resetAnim()

        // Apply transformations
        for (i in 0 until frame.translatorCount) {
            val index = frame.indexFrameIds[i]
            val tf = Transformation(
                frameMap.types[index], frameMap.frameMaps[index],
                frame.translator_x[i], frame.translator_y[i], frame.translator_z[i]
            )

            context.framebuffer.jointRenderer.addJoint(def, tf)
            def.animate(tf.type, tf.frameMap, tf.dx, tf.dy, tf.dz)
        }

        // Load transformed model
        context.loader.cleanUp()
        entity.model = context.datLoader.parse(def, context.framebuffer.shadingType == ShadingType.FLAT)
    }

    fun resetAnimation() {
        currentSequence = SequenceDefinition(-1)
        frameCount = 0
        frameLength = 0
        context.gui.animationPanel.stop()
    }

    class Transformation(val type: Int, val frameMap: IntArray, val dx: Int, val dy: Int, val dz: Int)
}