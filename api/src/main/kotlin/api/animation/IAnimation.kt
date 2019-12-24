package api.animation

import net.runelite.cache.definitions.SequenceDefinition

interface IAnimation {
    val keyframes: List<IKeyframe>
    fun toSequence(archiveId: Int): SequenceDefinition
}