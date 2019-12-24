package api.animation

import api.definition.SequenceDefinition

interface IAnimation {
    val keyframes: List<IKeyframe>
    fun toSequence(archiveId: Int): SequenceDefinition
}