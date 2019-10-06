import net.runelite.cache.definitions.loaders.SequenceLoader
import org.displee.CacheLibrary
import org.junit.jupiter.api.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestCodec {

    private lateinit var library: CacheLibrary

    @BeforeAll
    fun before() {
        library = CacheLibrary("/Users/fred/Documents/PoserGL/repository/cache") // TODO: need?
    }

    @Test
    fun `sequence encoding consistent`() {
        val id = 1
        val raw = byteArrayOf(
            1, 0, 11, 0, 4, 0, 4, 0, 10, 0, 4, 0, 10, 0, 2, 0, 6, 0, 4, 0, 3, 0, 2, 0, 4, 0, -101, 0, -98, 0, -97, 0,
            -96, 0, -95, 0, -94, 0, -93, 0, -92, 0, -91, 0, -100, 0, -99, 0, -23, 0, -23, 0, -23, 0, -23, 0, -23, 0,
            -23, 0, -23, 0, -23, 0, -23, 0, -23, 0, -23, 0
        )

        val sequence = SequenceLoader().load(id, raw)
        val encoded = CachePackerOSRS().encodeSequence(sequence)
        Assertions.assertArrayEquals(raw, encoded)
    }

    @Test
    fun `keyframe encoding consistent`() {
        // TODO
    }

    @AfterAll
    fun after() {
        library.close()
    }
}
