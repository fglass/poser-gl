package api

import net.runelite.cache.definitions.SequenceDefinition
import org.displee.CacheLibrary

interface ICachePacker {

    fun packFrames(library: CacheLibrary, animation: IAnimation): Int

    fun packSequence(library: CacheLibrary, sequence: SequenceDefinition) {
        error("Method not implemented")
    }

    fun packSequence(library: CacheLibrary, sequence: SequenceDefinition, maxAnimationId: Int) {
        error("Method not implemented")
    }

    fun getSequenceConfigIndex(): Int
}