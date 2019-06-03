package model

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

class AnimationHandler {

    private val sequences = java.util.HashMap<Int, SequenceDefinition>()
    private val frames = HashMultimap.create<Int, FrameDefinition>()

    private var sequenceDefinition: SequenceDefinition = SequenceDefinition(-1)
    private var frameCount = 0
    private var frameLength = 0

    init {
        val store = Store(File(CACHE_PATH))
        store.load()
        loadSequences(store)
        loadFrames(store)
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

    fun loadAnimation(id: Int) {
        sequenceDefinition = sequences[id] ?: return
        frameCount = 0
        frameLength = sequenceDefinition.frameLenghts[0]
    }

    fun tickAnimation(context: Processor) {
        if (context.entities.isEmpty() || sequenceDefinition.id == -1) {
            return
        }

        if (frameLength-- <= 0) {
            frameCount++
            frameLength = sequenceDefinition.frameLenghts[frameCount % sequenceDefinition.frameIDs.size]
        }

        val seqFrameId = sequenceDefinition.frameIDs[frameCount % sequenceDefinition.frameIDs.size]
        val frames = frames.get(seqFrameId.ushr(16))
        val frameFileId = seqFrameId and 0xFFFF

        val first = frames.stream().filter { frame -> frame.id == frameFileId }.findFirst()
        val frame = first.get()
        applyAnimation(frame, context)
    }

    private fun applyAnimation(frame: FrameDefinition, context: Processor) {
        val frameMap = frame.framemap
        animOffsetX = 0
        animOffsetY = 0
        animOffsetZ = 0

        for (entity in context.entities) {
            entity.rawModel.definition.resetAnim()
        }

        for (i in 0 until frame.translatorCount) {
            val type = frame.indexFrameIds[i]
            val fmType = frameMap.types[type]
            val fm = frameMap.frameMaps[type]
            val dx = frame.translator_x[i]
            val dy = frame.translator_y[i]
            val dz = frame.translator_z[i]

            for (entity in context.entities) {
                val def = entity.rawModel.definition
                def.animate(fmType, fm, dx, dy, dz)
                entity.rawModel = context.datLoader.parse(def, context.shading == ShadingType.FLAT, context.loader)
            }
        }
    }
}