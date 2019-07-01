package cache

enum class IndexType(val id: Int) {
    FRAME(0),
    FRAME_MAP(1),
    CONFIG(2),
    MODEL(7)
}

enum class ConfigType(val id: Int) {
    NPC(9),
    ITEM(10),
    SEQUENCE(12)
}