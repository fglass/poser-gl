import api.IAnimation
import api.ICachePacker
import api.ProgressListenerWrapper
import org.displee.CacheLibrary

class CachePacker667: ICachePacker {

    override fun toString() = "667"

    override fun packAnimation(animation: IAnimation, library: CacheLibrary, listener: ProgressListenerWrapper,
                               maxAnimationId: Int) {
        TODO("not implemented")
    }

    override fun getMaxFrameArchive(library: CacheLibrary): Int {
        TODO("not implemented")
    }
}