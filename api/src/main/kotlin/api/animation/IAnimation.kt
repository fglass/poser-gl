package api.animation

import api.definition.SequenceDef

interface IAnimation {
    val keyframes: List<IKeyframe>
    fun toSequence(archiveId: Int): SequenceDef
}