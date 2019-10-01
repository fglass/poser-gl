import animation.Animation
import cache.PluginLoader
import cache.ProgressListener
import gui.component.ProgressDialog
import org.displee.CacheLibrary
import render.RenderContext
import kotlin.test.assertEquals

class TestCodec {

    @Test fun `decoding matches encoding`() {
        val (loaders, packers) = PluginLoader.load() // TODO: test plugin
        val loader = loaders.first()
        val packer = packers.first()

        val library = CacheLibrary("/Users/fred/Documents/PoserGL/repository/cache") // TODO: test cache
        val archiveId = 0
        val before = library.getIndex(0).getArchive(archiveId).getFile(0).data

        val frames = loader.loadFrameArchives(library) // TODO
        val sequences = loader.loadSequences(library)
        val animation = Animation(MockContext(), sequences.first())

        val dialog = ProgressDialog("", "", MockContext())
        val listener = ProgressListener(dialog)
        packer.packAnimation(animation, archiveId, library, listener, -1)
        val after = library.getIndex(0).getArchive(archiveId).getFile(0).data

        assertEquals(before, after)
        library.close()
    }
}

//class MockContext: IRenderContext()