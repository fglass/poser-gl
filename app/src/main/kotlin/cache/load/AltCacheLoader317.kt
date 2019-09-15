package cache.load

import render.RenderContext
import animation.Animation
import cache.CacheService
import cache.IndexType
import mu.KotlinLogging
import net.runelite.cache.definitions.FrameDefinition
import net.runelite.cache.definitions.ItemDefinition
import net.runelite.cache.definitions.NpcDefinition
import net.runelite.cache.definitions.SequenceDefinition
import org.displee.CacheLibrary
import java.util.HashSet

private val logger = KotlinLogging.logger {}

class AltCacheLoader317(private val context: RenderContext): ICacheLoader {

    private val originalLoader = CacheLoader317(context)

    override fun loadSequences(library: CacheLibrary): HashMap<Int, Animation> {
        val archive = library.getIndex(IndexType.CONFIG.id317)
            .getArchive(IndexType.SEQUENCE.id317)
            .getFile("seq.dat")

        val stream = InputStream317(archive.data)
        val length = stream.readUShort()
        val sequences = HashMap<Int, Animation>()

        for (i in 0 until length) {
            val animation = Animation(context, decodeSequence(SequenceDefinition(i), stream))
            if (animation.sequence.frameIDs != null) {
                sequences[i] = animation
            } else {
                logger.info { "Sequence $i contains no frames" }
            }
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

    override fun loadFrameArchive(archiveId: Int, library: CacheLibrary): HashSet<FrameDefinition> {
        return originalLoader.loadFrameArchive(archiveId, library)
    }

    override fun loadNpcDefintions(library: CacheLibrary): HashMap<Int, NpcDefinition> {
        return originalLoader.loadNpcDefintions(library)
    }

    override fun loadItemDefinitions(library: CacheLibrary): HashMap<Int, ItemDefinition> {
        return originalLoader.loadItemDefinitions(library)
    }
}