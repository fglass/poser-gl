import api.ICacheLoader
import com.google.common.collect.HashMultimap
import net.runelite.cache.definitions.*
import org.displee.CacheLibrary
import java.util.HashMap

class CacheLoader667: ICacheLoader {

    override fun toString() = "667"

    override fun loadSequences(library: CacheLibrary): List<SequenceDefinition> {
        println("Loading sequences")
        TODO("not implemented")
    }

    override fun loadFrameArchives(library: CacheLibrary): HashMultimap<Int, FrameDefinition> {
        TODO("not implemented")
    }

    override fun loadNpcDefinitions(library: CacheLibrary): HashMap<Int, NpcDefinition> {
        TODO("not implemented")
    }

    override fun loadItemDefinitions(library: CacheLibrary): HashMap<Int, ItemDefinition> {
        TODO("not implemented")
    }

    override fun loadModelDefinition(library: CacheLibrary, modelId: Int): ModelDefinition {
        TODO("not implemented")
    }
}