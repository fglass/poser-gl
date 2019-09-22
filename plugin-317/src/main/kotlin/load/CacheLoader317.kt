package load

import InputStream317
import com.google.common.collect.HashMultimap
import net.runelite.cache.definitions.*
import org.displee.CacheLibrary

const val FRAME_INDEX = 2
const val CONFIG_INDEX = 0
const val NPC_INDEX = 2
const val ITEM_INDEX = 2
const val SEQUENCE_INDEX = 2

class CacheLoader317: ICacheLoader {

    override fun toString() = "317"

    override fun loadSequences(library: CacheLibrary): List<SequenceDefinition> {
        val archive = library.getIndex(CONFIG_INDEX).getArchive(SEQUENCE_INDEX).getFile("seq.dat")
        val stream = InputStream317(archive.data)
        val length = stream.readUShort()
        val sequences = ArrayList<SequenceDefinition>()

        for (i in 0 until length) {
            sequences.add(decodeSequence(SequenceDefinition(i), stream))
        }
        return sequences
    }

    // OSRS decoding
    private fun decodeSequence(def: SequenceDefinition, stream: InputStream317): SequenceDefinition {
        while (true) {
            when (stream.readUnsignedByte()) {
                0 -> return def
                1 -> {
                    val length = stream.readUShort()
                    def.frameIDs = IntArray(length)
                    def.frameLenghts = IntArray(length)

                    for (i in 0 until length) {
                        def.frameLenghts[i] = stream.readUShort()
                    }

                    for (i in 0 until length) {
                        def.frameIDs[i] = stream.readUShort()
                    }

                    for (i in 0 until length) {
                        def.frameIDs[i] += stream.readUShort() shl 16
                    }
                }
                2 -> def.frameStep = stream.readUShort()
                3 -> {
                    val length = stream.readUnsignedByte()
                    def.interleaveLeave = IntArray(length + 1)
                    for (i in 0 until length) {
                        def.interleaveLeave[i] = stream.readUnsignedByte()
                    }
                    def.interleaveLeave[length] = 9999999
                }
                4 -> def.stretches = true
                5 -> def.forcedPriority = stream.readUnsignedByte()
                6 -> def.leftHandItem = stream.readUShort()
                7 -> def.rightHandItem = stream.readUShort()
                8 -> def.maxLoops = stream.readUnsignedByte()
                9 -> def.precedenceAnimating = stream.readUnsignedByte()
                10 -> def.priority = stream.readUnsignedByte()
                11 -> def.replyMode = stream.readUnsignedByte()
                12 -> {
                    val length = stream.readUnsignedByte()
                    for (i in 0 until length) {
                        stream.readUShort()
                    }

                    for (i in 0 until length) {
                        stream.readUShort()
                    }
                }
                13 -> {
                    val length = stream.readUnsignedByte()
                    for (i in 0 until length) {
                        stream.readUTriByte()
                    }
                }
            }
        }
    }

    override fun loadFrameArchives(library: CacheLibrary): HashMultimap<Int, FrameDefinition> {
        val frames: HashMultimap<Int, FrameDefinition> = HashMultimap.create()
        for (i in 0..library.getIndex(FRAME_INDEX).lastArchive.id) {
            val file = library.getIndex(FRAME_INDEX).getArchive(i).getFile(0)
            if (file.data.isNotEmpty()) {
                try {
                    decodeFrameArchive(i, file.data, frames)
                } catch (e: ArrayIndexOutOfBoundsException) {
                    //logger.error { "Archive $i could not be fully loaded" } TODO
                }
            }
        }
        return frames
    }

    private fun decodeFrameArchive(archiveId: Int, data: ByteArray, frames: HashMultimap<Int, FrameDefinition>) {
        val stream = InputStream317(data)
        val frameMap = decodeFrameMap(stream)

        val fileLength = stream.readUShort()
        val indexFrameIds = IntArray(500)
        val scratchTranslatorX = IntArray(500)
        val scratchTranslatorY = IntArray(500)
        val scratchTranslatorZ = IntArray(500)

        repeat(fileLength) {
            val frameIndex = stream.readUShort()
            val def = FrameDefinition()
            def.id = frameIndex
            frameMap.id = frameIndex
            def.framemap = frameMap

            val length = stream.readUnsignedByte()
            var lastI = -1
            var index = 0

            for (i in 0 until length) {
                val type = stream.readUnsignedByte()
                if (type > 0) {
                    if (frameMap.types[i] != 0) {
                        for (var10 in i - 1 downTo lastI + 1) {
                            if (frameMap.types[var10] == 0) {
                                indexFrameIds[index] = var10
                                scratchTranslatorX[index] = 0
                                scratchTranslatorY[index] = 0
                                scratchTranslatorZ[index] = 0
                                ++index
                                break
                            }
                        }
                    }
                    indexFrameIds[index] = i
                    val value = if (frameMap.types[i] == 3) 128 else 0

                    if ((type and 1) != 0) {
                        scratchTranslatorX[index] = stream.readShort2()
                    } else {
                        scratchTranslatorX[index] = value
                    }
                    if ((type and 2) != 0) {
                        scratchTranslatorY[index] = stream.readShort2()
                    } else {
                        scratchTranslatorY[index] = value
                    }
                    if ((type and 4) != 0) {
                        scratchTranslatorZ[index] = stream.readShort2()
                    } else {
                        scratchTranslatorZ[index] = value
                    }
                    lastI = i
                    ++index
                }
            }

            def.translatorCount = index
            def.indexFrameIds = IntArray(index)
            def.translator_x = IntArray(index)
            def.translator_y = IntArray(index)
            def.translator_z = IntArray(index)

            for (i in 0 until index) {
                def.indexFrameIds[i] = indexFrameIds[i]
                def.translator_x[i] = scratchTranslatorX[i]
                def.translator_y[i] = scratchTranslatorY[i]
                def.translator_z[i] = scratchTranslatorZ[i]
            }
            frames.put(archiveId, def)
        }
    }

    private fun decodeFrameMap(stream: InputStream317): FramemapDefinition {
        val def = FramemapDefinition()
        def.id = -1 // Unused
        def.length = stream.readUShort()
        def.types = IntArray(def.length)
        def.frameMaps = arrayOfNulls(def.length)

        for (i in 0 until def.length) {
            def.types[i] = stream.readUShort()
        }

        for (i in 0 until def.length) {
            def.frameMaps[i] = IntArray(stream.readUShort())
        }

        for (i in 0 until def.length) {
            for (j in 0 until def.frameMaps[i].size) {
                def.frameMaps[i][j] = stream.readUShort()
            }
        }
        return def
    }

    override fun loadNpcDefinitions(library: CacheLibrary): HashMap<Int, NpcDefinition> {
        val entities = HashMap<Int, NpcDefinition>()
        val npcIdx = library.getIndex(CONFIG_INDEX).getArchive(NPC_INDEX).getFile("npc.idx")
        val npcArchive = library.getIndex(CONFIG_INDEX).getArchive(NPC_INDEX).getFile("npc.dat")

        val idxStream = InputStream317(npcIdx.data)
        val total = idxStream.readUShort()

        val streamIndices = IntArray(total)
        var offset = 2
        for (i in 0 until total) {
            streamIndices[i] = offset
            offset += idxStream.readUShort()
        }

        val stream = InputStream317(npcArchive.data)
        for (i in 0 until total) {
            stream.currentPosition = streamIndices[i]
            val npc = decodeNpcDefinition(i, stream)
            if (npc.models != null && npc.name.toLowerCase() != "null" && npc.name != "") {
                entities[npc.id] = npc
            }
        }
        return entities
    }

    private fun decodeNpcDefinition(id: Int, stream: InputStream317): NpcDefinition {
        val def = NpcDefinition(id)
        while (true) {
            val opCode = stream.readUnsignedByte()
            if (opCode == 0)
                return def
            if (opCode == 1) {
                val j = stream.readUnsignedByte()
                def.models = IntArray(j)
                for (j1 in 0 until j)
                    def.models[j1] = stream.readUShort()

            } else if (opCode == 2)
                def.name = stream.readString()
            else if (opCode == 3) {
                stream.readBytes()
            } else if (opCode == 12)
                def.tileSpacesOccupied = stream.readSignedByte().toInt()
            else if (opCode == 13)
                def.stanceAnimation = stream.readUShort()
            else if (opCode == 14)
                def.walkAnimation = stream.readUShort()
            else if (opCode == 17) {
                def.walkAnimation = stream.readUShort()
                def.rotate180Animation = stream.readUShort()
                def.rotate90RightAnimation = stream.readUShort()
                def.rotate90LeftAnimation = stream.readUShort()
            } else if (opCode in 30..39) {
                if (def.options == null)
                    def.options = arrayOfNulls<String>(5)
                def.options[opCode - 30] = stream.readString()
                if (def.options[opCode - 30].equals("hidden", ignoreCase = true))
                    def.options[opCode - 30] = null
            } else if (opCode == 40) {
                val colours = stream.readUnsignedByte()
                def.recolorToFind = ShortArray(colours)
                def.recolorToReplace = ShortArray(colours)
                for (k1 in 0 until colours) {
                    def.recolorToFind[k1] = stream.readUShort().toShort()
                    def.recolorToReplace[k1] = stream.readUShort().toShort()
                }

            } else if (opCode == 60) {
                val additionalModelLen = stream.readUnsignedByte()
                def.models_2 = IntArray(additionalModelLen)
                for (l1 in 0 until additionalModelLen)
                    def.models_2[l1] = stream.readUShort()

            } else if (opCode == 90)
                stream.readUShort()
            else if (opCode == 91)
                stream.readUShort()
            else if (opCode == 92)
                stream.readUShort()
            else if (opCode == 93)
                def.renderOnMinimap = false
            else if (opCode == 95)
                def.combatLevel = stream.readUShort()
            else if (opCode == 97)
                def.resizeX = stream.readUShort()
            else if (opCode == 98)
                def.resizeY = stream.readUShort()
            else if (opCode == 99)
                def.hasRenderPriority = true
            else if (opCode == 100)
                def.ambient = stream.readSignedByte().toInt()
            else if (opCode == 101)
                def.contrast = stream.readSignedByte() * 5
            else if (opCode == 102)
                def.headIcon = stream.readUShort()
            else if (opCode == 103)
                def.rotation = stream.readUShort()
            else if (opCode == 106) {
                def.varbitIndex = stream.readUShort()
                if (def.varbitIndex == 65535)
                    def.varbitIndex = -1
                def.varpIndex = stream.readUShort()
                if (def.varpIndex == 65535)
                    def.varpIndex = -1
                val childCount = stream.readUnsignedByte()
                def.configs = IntArray(childCount + 1)
                for (i2 in 0..childCount) {
                    def.configs[i2] = stream.readUShort()
                    if (def.configs[i2] == 65535)
                        def.configs[i2] = -1
                }
            } else if (opCode == 107)
                def.isClickable = false
        }
    }

    override fun loadItemDefinitions(library: CacheLibrary): HashMap<Int, ItemDefinition> {
        val items = HashMap<Int, ItemDefinition>()
        val itemIdx = library.getIndex(CONFIG_INDEX).getArchive(ITEM_INDEX).getFile("obj.idx")
        val itemArchive = library.getIndex(CONFIG_INDEX).getArchive(ITEM_INDEX).getFile("obj.dat")

        val idxStream = InputStream317(itemIdx.data)
        val total = idxStream.readUShort()

        val streamIndices = IntArray(total)
        var offset = 2
        for (i in 0 until total) {
            streamIndices[i] = offset
            offset += idxStream.readUShort()
        }

        val stream = InputStream317(itemArchive.data)
        for (i in 0 until total) {
            stream.currentPosition = streamIndices[i]
            val item = decodeItemDefinition(i, stream)
            if (item.maleModel0 > 0 && item.name.toLowerCase() != "null") {
                items[item.id] = item
            }
        }
        return items
    }

    private fun decodeItemDefinition(id: Int, stream: InputStream317): ItemDefinition {
        val def = ItemDefinition(id)
        while (true) {
            val opCode = stream.readUnsignedByte()
            if (opCode == 0)
                return def
            if (opCode == 1)
                def.inventoryModel = stream.readUShort()
            else if (opCode == 2)
                def.name = stream.readString()
            else if (opCode == 3) {
                stream.readString()
            } else if (opCode == 4)
                def.zoom2d = stream.readUShort()
            else if (opCode == 5)
                def.xan2d = stream.readUShort()
            else if (opCode == 6)
                def.yan2d = stream.readUShort()
            else if (opCode == 7) {
                def.xOffset2d = stream.readUShort()
                if (def.xOffset2d > 32767)
                    def.xOffset2d -= 0x10000
            } else if (opCode == 8) {
                def.yOffset2d = stream.readUShort()
                if (def.yOffset2d > 32767)
                    def.yOffset2d -= 0x10000
            } else if (opCode == 10)
                stream.readUShort()
            else if (opCode == 11)
                def.stackable = 1
            else if (opCode == 12) {
                def.cost = stream.readInt()
            } else if (opCode == 16)
                def.members = true
            else if (opCode == 23) {
                def.maleModel0 = stream.readUShort()
                def.maleOffset = stream.readSignedByte().toInt()
            } else if (opCode == 24)
                def.maleModel1 = stream.readUShort()
            else if (opCode == 25) {
                def.femaleModel0 = stream.readUShort()
                def.femaleOffset = stream.readSignedByte().toInt()
            } else if (opCode == 26)
                def.femaleModel1 = stream.readUShort()
            else if (opCode in 30..34) {
                if (def.options == null)
                    def.options = arrayOfNulls<String>(5)
                def.options[opCode - 30] = stream.readString()
                if (def.options[opCode - 30].equals("hidden", ignoreCase = true))
                    def.options[opCode - 30] = null
            } else if (opCode in 35..39) {
                if (def.interfaceOptions == null)
                    def.interfaceOptions = arrayOfNulls<String>(5)
                def.interfaceOptions[opCode - 35] = stream.readString()
            } else if (opCode == 40) {
                val j = stream.readUnsignedByte()
                def.colorFind = ShortArray(j)
                def.colorReplace = ShortArray(j)
                for (k in 0 until j) {
                    def.colorReplace[k] = stream.readUShort().toShort()
                    def.colorFind[k] = stream.readUShort().toShort()
                }
            } else if (opCode == 78)
                def.maleModel2 = stream.readUShort()
            else if (opCode == 79)
                def.femaleModel2 = stream.readUShort()
            else if (opCode == 90)
                def.maleHeadModel = stream.readUShort()
            else if (opCode == 91)
                def.femaleHeadModel = stream.readUShort()
            else if (opCode == 92)
                def.maleHeadModel2 = stream.readUShort()
            else if (opCode == 93)
                def.femaleHeadModel2 = stream.readUShort()
            else if (opCode == 95)
                def.zan2d = stream.readUShort()
            else if (opCode == 97)
                def.notedID = stream.readUShort()
            else if (opCode == 98)
                def.notedTemplate = stream.readUShort()
            else if (opCode in 100..109) {
                if (def.countObj == null) {
                    def.countObj = IntArray(10)
                    def.countCo = IntArray(10)
                }
                def.countObj[opCode - 100] = stream.readUShort()
                def.countCo[opCode - 100] = stream.readUShort()
            } else if (opCode == 110)
                def.resizeX = stream.readUShort()
            else if (opCode == 111)
                def.resizeY = stream.readUShort()
            else if (opCode == 112)
                def.resizeZ = stream.readUShort()
            else if (opCode == 113)
                def.ambient = stream.readSignedByte().toInt()
            else if (opCode == 114)
                def.contrast = stream.readSignedByte() * 5
            else if (opCode == 115)
                def.team = stream.readUnsignedByte()
        }
    }
}