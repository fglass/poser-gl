package api.cache

import api.animation.IAnimation
import api.definition.SequenceDefinition
import org.displee.CacheLibrary

interface ICachePacker {

    val sequenceConfigIndex: Int

    fun packFrames(library: CacheLibrary, animation: IAnimation): Int

    fun packSequence(library: CacheLibrary, sequence: SequenceDefinition) {
        error("Method not implemented")
    }

    fun packSequence(library: CacheLibrary, sequence: SequenceDefinition, maxAnimationId: Int) {
        error("Method not implemented")
    }
}