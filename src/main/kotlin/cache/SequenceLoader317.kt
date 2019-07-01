package cache

import Processor
import animation.Animation
import net.runelite.cache.definitions.SequenceDefinition
import net.runelite.cache.io.InputStream

class SequenceLoader317 {

    fun load(context: Processor, data: ByteArray): HashMap<Int, Animation> {
        val stream = InputStream(data)
        val length = stream.readUnsignedShort()
        val sequences = HashMap<Int, Animation>()

        for (i in 0 until length) {
            sequences[i] = Animation(context, decode(SequenceDefinition(i), stream))
        }

        println("Loaded $length 317 sequences")
        return sequences
    }

    private fun decode(def: SequenceDefinition, stream: InputStream): SequenceDefinition {
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
}