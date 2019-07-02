package cache

enum class IndexType(private val id: Pair<Int, Int>) {

    // Indices
    FRAME(Pair(0, 2)),
    FRAME_MAP(Pair(1, 2)),
    CONFIG(Pair(2, 0)),
    MODEL(Pair(7, 1)),
    // Configs
    NPC(Pair(9, 2)),
    ITEM(Pair(10, 2)),
    SEQUENCE(Pair(12, 2));

    fun getIndexId(osrs: Boolean): Int {
        return if (osrs) id.first else id.second
    }
}