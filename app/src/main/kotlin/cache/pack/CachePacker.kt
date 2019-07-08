package cache.pack

import animation.Animation
import cache.ProgressListener

interface CachePacker {
    fun packAnimation(animation: Animation, listener: ProgressListener)
}