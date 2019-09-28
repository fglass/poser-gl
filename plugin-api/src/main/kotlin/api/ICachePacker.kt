package api

import org.displee.CacheLibrary

interface ICachePacker {
    fun packAnimation(animation: IAnimation, archiveId: Int, library: CacheLibrary, listener: ProgressListenerWrapper,
                      maxAnimationId: Int)
}