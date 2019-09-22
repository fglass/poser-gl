package cache

enum class IndexType(val idOsrs: Int, val id317: Int) {  // TODO: remove
    // Indices
    FRAME(0, 2),
    FRAME_MAP(1, 2),
    CONFIG(2, 0),
    MODEL(7, 1),
    // Configs
    NPC(9, 2),
    ITEM(10, 2),
    SEQUENCE(12, 2);
}