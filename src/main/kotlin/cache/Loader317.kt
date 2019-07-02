package cache

import Processor
import animation.Animation
import net.runelite.cache.definitions.FrameDefinition
import net.runelite.cache.definitions.FramemapDefinition
import net.runelite.cache.definitions.ItemDefinition
import net.runelite.cache.definitions.SequenceDefinition
import net.runelite.cache.io.InputStream

class Loader317 {

    fun loadSequences(context: Processor, data: ByteArray): HashMap<Int, Animation> {
        val stream = InputStream(data)
        val length = stream.readUnsignedShort()
        val sequences = HashMap<Int, Animation>()

        for (i in 0 until length) {
            sequences[i] = Animation(context, decodeSequence(SequenceDefinition(i), stream))
        }

        println("Loaded $length 317 sequences")
        return sequences
    }

    private fun decodeSequence(def: SequenceDefinition, stream: InputStream): SequenceDefinition {
        while (true) {
            when (stream.readUnsignedByte()) {
                0 -> return def
                1 -> {
                    val length = stream.readUnsignedShort()
                    def.frameIDs = IntArray(length)
                    for (i in 0 until length) {
                        def.frameIDs[i] = stream.readInt()
                    }

                    def.frameLenghts = IntArray(length)
                    for (i in 0 until length) {
                        def.frameLenghts[i] = stream.readUnsignedByte()
                    }
                }
                2 -> def.frameStep = stream.readUnsignedShort()
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
                6 -> def.leftHandItem = stream.readUnsignedShort()
                7 -> def.rightHandItem = stream.readUnsignedShort()
                8 -> def.maxLoops = stream.readUnsignedByte()
                9 -> def.precedenceAnimating = stream.readUnsignedByte()
                10 -> def.priority = stream.readUnsignedByte()
                11 -> def.replyMode = stream.readUnsignedByte()
                12 -> stream.readInt()
            }
        }
    }

    fun loadFrames(archive: Int, data: ByteArray, service: CacheService) {
        val stream = InputStream(data)
        val frameMap = getFrameMap(-1, stream)

        val frames = stream.readUnsignedShort()
        //animationlist[archive] = arrayOfNulls<Frame>(n * 3)
        //frames[archive] = MutableSet<FrameDefinition>(n * 3)
        val indexFrameIds = IntArray(500)
        val scratchTranslatorX = IntArray(500)
        val scratchTranslatorY = IntArray(500)
        val scratchTranslatorZ = IntArray(500)

        for (j in 0 until frames) {
            val framemapArchiveIndex = stream.readUnsignedShort()
            frameMap.id = framemapArchiveIndex // TODO
            val def = FrameDefinition()
            def.id = j

            //animationlist[archive][framemapArchiveIndex] = def
            def.framemap = frameMap

            val length = stream.readUnsignedByte()
            println("Length: $length")
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

                    if (type and 1 != 0) {
                        scratchTranslatorX[index] = stream.readShortSmart()
                    } else {
                        scratchTranslatorX[index] = value
                    }
                    if (type and 2 != 0) {
                        scratchTranslatorY[index] = stream.readShortSmart()
                    } else {
                        scratchTranslatorY[index] = value
                    }
                    if (type and 3 != 0) {
                        scratchTranslatorZ[index] = stream.readShortSmart()
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
            service.frames.put(archive, def)
            println("Adding $archive ${def.id}")
        }
    }

    private fun getFrameMap(id: Int, stream: InputStream): FramemapDefinition {
        val def = FramemapDefinition()
        //val stream = InputStream(b)
        def.id = id
        def.length = stream.readUnsignedShort()
        def.types = IntArray(def.length)
        def.frameMaps = arrayOfNulls(def.length)

        var i = 0
        while (i < def.length) {
            def.types[i] = stream.readUnsignedShort()
            ++i
        }

        i = 0
        while (i < def.length) {
            def.frameMaps[i] = IntArray(stream.readUnsignedShort())
            ++i
        }

        i = 0
        while (i < def.length) {
            for (j in 0 until def.frameMaps[i].size) {
                def.frameMaps[i][j] = stream.readUnsignedShort()
            }
            ++i
        }

        return def
    }

    fun loadItem(id: Int, stream: InputStream317): ItemDefinition {
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
                val description = stream.readString()
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
            else if (opCode >= 30 && opCode < 35) {
                if (def.options == null)
                    def.options = arrayOfNulls<String>(5)
                def.options[opCode - 30] = stream.readString()
                if (def.options[opCode - 30].equals("hidden", ignoreCase = true))
                    def.options[opCode - 30] = null
            } else if (opCode >= 35 && opCode < 40) {
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
            else if (opCode >= 100 && opCode < 110) {

                if (def.countObj == null) {
                    def.countObj = IntArray(10)
                    def.countCo = IntArray(10)
                }
                def.countObj[opCode - 100] = stream.readUShort()
                def.countCo[opCode - 100] = stream.readUShort()

                /*int length = stream.readUnsignedByte();
				stack_variant_id = new int [length];
				stack_variant_size = new int[length];
				for (int i2 = 0; i2< length; i2++) {
					stack_variant_id[i2] = stream.readUnsignedShort();
					stack_variant_size[i2] = stream.readUnsignedShort();
				}*/
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