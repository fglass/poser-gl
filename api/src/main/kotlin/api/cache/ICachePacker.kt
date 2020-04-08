package api.cache

import api.animation.IAnimation
import api.definition.SequenceDefinition

interface ICachePacker {

    val sequenceConfigIndex: Int

    fun packFrames(library: ICacheLibrary, animation: IAnimation): Int

    fun packSequence(library: ICacheLibrary, sequence: SequenceDefinition) {
        error("Method not implemented")
    }

    fun packSequence(library: ICacheLibrary, sequence: SequenceDefinition, maxAnimationId: Int) {
        error("Method not implemented")
    }
}