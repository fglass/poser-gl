package util

import org.liquidengine.legui.component.Component

fun Component.setSizeLimits(width: Float, height: Float) {
    style.setMinimumSize(width, height)
    style.setMaximumSize(width, height)
}

fun Component.setHeightLimit(limit: Float) {
    style.setMinHeight(limit)
    style.setMaxHeight(limit)
}