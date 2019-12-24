package api.animation

import net.runelite.cache.definitions.FramemapDefinition

interface IKeyframe {
    val modified: Boolean
    val frameMap: FramemapDefinition
    val transformations: List<ITransformation>
}