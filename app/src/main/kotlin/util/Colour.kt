package util

import org.joml.Vector4f
import org.liquidengine.legui.style.color.ColorConstants
import org.liquidengine.legui.style.color.ColorUtil
import java.util.*

enum class Colour(val rgba: Vector4f) {
    GRAY(ColorUtil.fromInt(33, 33, 33, 1f)),
    BLACK(ColorConstants.black()),
    WHITE(ColorConstants.white()),
    RED(ColorConstants.lightRed()),
    GREEN(ColorConstants.lightGreen()),
    BLUE(ColorConstants.lightBlue());

    override fun toString() = super.toString()
        .lowercase(Locale.getDefault())
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}