package cache.load

import cache.IndexType
import com.google.common.collect.HashMultimap
import net.runelite.cache.definitions.FrameDefinition
import net.runelite.cache.definitions.ItemDefinition
import net.runelite.cache.definitions.NpcDefinition
import net.runelite.cache.definitions.SequenceDefinition
import org.displee.CacheLibrary

class CacheLoader317: ICacheLoader {

    private val originalLoader = LegacyCacheLoader317()

    override fun loadSequences(library: CacheLibrary): List<SequenceDefinition> {
        val archive = library.getIndex(IndexType.CONFIG.id317)
            .getArchive(IndexType.SEQUENCE.id317)
            .getFile("seq.dat")

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
        return originalLoader.loadFrameArchives(library)
    }

    override fun loadNpcDefinitions(library: CacheLibrary): HashMap<Int, NpcDefinition> {
        return originalLoader.loadNpcDefinitions(library)
    }

    override fun loadItemDefinitions(library: CacheLibrary): HashMap<Int, ItemDefinition> {
        return originalLoader.loadItemDefinitions(library)
    }
}