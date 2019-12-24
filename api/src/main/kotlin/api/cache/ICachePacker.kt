package api.cache

import api.animation.IAnimation
import api.definition.SequenceDef
import org.displee.CacheLibrary

interface ICachePacker {

    val sequenceConfigIndex: Int

    fun packFrames(library: CacheLibrary, animation: IAnimation): Int

    fun packSequence(library: CacheLibrary, sequence: SequenceDef) {
        error("Method not implemented")
    }

    fun packSequence(library: CacheLibrary, sequence: SequenceDef, maxAnimationId: Int) {
        error("Method not implemented")
    }
}