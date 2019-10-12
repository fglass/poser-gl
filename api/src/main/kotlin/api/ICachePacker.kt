package api

import org.displee.CacheLibrary

interface ICachePacker { // TODO: just encoding methods instead?

    fun packAnimation(animation: IAnimation, library: CacheLibrary, listener: ProgressListenerWrapper,
                      maxAnimationId: Int)

    fun getMaxFrameArchive(library: CacheLibrary): Int
}