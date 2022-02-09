package util

import org.joml.Vector4f
import com.spinyowl.legui.style.color.ColorConstants
import com.spinyowl.legui.style.color.ColorUtil

enum class Colour(val rgba: Vector4f) {
    GRAY(ColorUtil.fromInt(33, 33, 33, 1f)),
    BLACK(ColorConstants.black()),
    WHITE(ColorConstants.white()),
    RED(ColorConstants.lightRed()),
    GREEN(ColorConstants.lightGreen()),
    BLUE(ColorConstants.lightBlue());

    override fun toString() = super.toString().toLowerCase().capitalize()
}