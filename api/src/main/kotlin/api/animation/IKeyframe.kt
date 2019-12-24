package api.animation

import api.definition.FrameMapDefinition

interface IKeyframe {
    val modified: Boolean
    val frameMap: FrameMapDefinition
    val transformations: List<ITransformation>
}