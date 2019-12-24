package api.animation

import api.definition.FrameMapDef

interface IKeyframe {
    val modified: Boolean
    val frameMap: FrameMapDef
    val transformations: List<ITransformation>
}