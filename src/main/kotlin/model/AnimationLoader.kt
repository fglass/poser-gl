package model

import CACHE_PATH
import net.runelite.cache.ConfigType
import net.runelite.cache.IndexType
import net.runelite.cache.definitions.FrameDefinition
import net.runelite.cache.definitions.ModelDefinition
import net.runelite.cache.definitions.loaders.FrameLoader
import net.runelite.cache.definitions.loaders.FramemapLoader
import net.runelite.cache.definitions.loaders.SequenceLoader
import net.runelite.cache.fs.Store
import render.Loader
import java.io.File

class AnimationLoader {

    private val map = HashMap<Int, ArrayList<FrameDefinition>>()

    init {
        val base = File(CACHE_PATH)

        Store(base).use { store ->
            store.load()

            val storage = store.storage
            val frameIndex = store.getIndex(IndexType.FRAMES)
            val framemapIndex = store.getIndex(IndexType.FRAMEMAPS)

            for (archive in frameIndex.archives) {
                var archiveData = storage.loadArchive(archive)
                val archiveFiles = archive.getFiles(archiveData)
                map[archive.archiveId] = ArrayList()

                for (archiveFile in archiveFiles.files) {
                    val contents = archiveFile.contents

                    val framemapArchiveId = (contents[0].toInt() and 0xff) shl 8 or (contents[1].toInt() and 0xff)

                    val framemapArchive = framemapIndex.archives[framemapArchiveId]
                    archiveData = storage.loadArchive(framemapArchive)
                    val framemapContents = framemapArchive.decompress(archiveData)

                    val fmloader = FramemapLoader()
                    val framemap = fmloader.load(framemapArchive.archiveId, framemapContents)

                    val frameLoader = FrameLoader()
                    val frame = frameLoader.load(framemap, archiveFile.fileId, contents)

                    map[archive.archiveId]?.add(frame)
                }
            }
        }
    }

    fun loadAnimation(id: Int, model: ModelDefinition, loader: Loader) {
        Store(File(CACHE_PATH)).use { store ->
            store.load()

            val storage = store.storage
            val index = store.getIndex(IndexType.CONFIGS)
            val archive = index.getArchive(ConfigType.SEQUENCE.id)

            val archiveData = storage.loadArchive(archive)
            val files = archive.getFiles(archiveData)

            val file = files.files[id]
            val seqLoader = SequenceLoader()
            val seq = seqLoader.load(file.fileId, file.contents)

            println("Frames: ${seq.frameIDs.size}")
            seq.frameIDs.forEach {
                val frameId = it shr 16
                println("Loading frame: $frameId")
                val frames = map[frameId]
                frames?.forEach { f ->

                    for (i in 0 until f.translatorCount) {

                        val tIndex = f.indexFrameIds[i]
                        val tX = f.translator_x[tIndex]
                        val tY = f.translator_y[tIndex]
                        val tZ = f.translator_z[tIndex]
                        val skins = f.framemap.frameMaps[tIndex]

                        for (j in 0 until skins.size) {
                            val skin = skins[j]
                            /*val vertexGroup = model.anIntArrayArray1759[skin]

                            for (z in 0 until vertexGroup.size) {
                                val vIndex = vertexGroup[z]
                                model.vertexX[vIndex] += tX
                                model.vertexY[vIndex] += tY
                                model.vertexZ[vIndex] += tZ
                            }*/
                        }
                    }
                }
                DatLoader().parse(model, false, loader)
            }
        }
    }
}