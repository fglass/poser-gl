package util

import org.liquidengine.legui.image.BufferedImage

object ResourceMap {

    private const val SPRITE_PATH = "sprite/"
    private val iconMap = mutableMapOf<String, BufferedImage>()

    init {
        put("add", hovered = true)
        put("copy", hovered = true)
        put("paste", hovered = true)
        put("lerp", hovered = true)
        put("trash", hovered = true)

        put("translation")
        put("rotation")
        put("scale")

        put("left")
        put("right")

        put("play", hovered = true)
        put("pause", hovered = true)
        put("info", hovered = true)
        put("node")
        put("node-toggled")

        put("model")
        put("delete")

        put("fill-cube")
        put("line-cube")
        put("point-cube")

        put("smooth-shading")
        put("flat-shading")
        put("no-shading")

        put("grey-line")
        put("yellow-line")
        put("pink-line")
        put("green-line")
        put("blue-line")
        put("red-line")

        put("open", hovered = true)
        put("load", hovered = true)
        put("title")
        put("icon")

        put("home", hovered = true)
        put("pack", hovered = true)
        put("export", hovered = true)
        put("import", hovered = true)
        put("undo", hovered = true)
        put("redo", hovered = true)
        put("settings", hovered = true)
    }

    private fun put(key: String, hovered: Boolean = false) {
        iconMap[key] = BufferedImage("${SPRITE_PATH}$key.png")
        if (hovered) {
            put("$key-hovered")
        }
    }

    operator fun get(key: String) = iconMap[key] ?: error("$key resource not found")
}