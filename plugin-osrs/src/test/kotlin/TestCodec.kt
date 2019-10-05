import net.runelite.cache.definitions.loaders.SequenceLoader
import org.displee.CacheLibrary
import org.junit.jupiter.api.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestCodec {

    private lateinit var library: CacheLibrary

    @BeforeAll
    fun before() {
        library = CacheLibrary("/Users/fred/Documents/PoserGL/repository/cache") // TODO: test cache
    }

    @Test
    fun `sequence decoding matches encoding`() {
        val file = library.getIndex(CONFIG_INDEX).getArchive(SEQUENCE_INDEX).getFile(14)
        val decoded = file.data

        val sequence = SequenceLoader().load(file.id, decoded)
        val encoded = CachePackerOSRS().encodeSequence(sequence)
        Assertions.assertArrayEquals(decoded, encoded)
    }

    @Test
    fun `keyframe decoding matches encoding`() {
        // TODO
    }

    @AfterAll
    fun after() {
        library.close()
    }
}
