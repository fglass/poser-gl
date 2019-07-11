package animation

import SPRITE_PATH
import Processor
import org.liquidengine.legui.image.BufferedImage
import kotlin.reflect.KFunction1

enum class KeyframeAction(private val action: KFunction1<Animation, Unit>, private val iconPath: String,
                          private val hoveredPath: String) {

    ADD(Animation::addKeyframe, "add", "add-hovered"),
    COPY(Animation::copyKeyframe, "copy", "copy-hovered"),
    PASTE(Animation::pasteKeyframe, "paste", "paste-hovered"),
    INTERPOLATE(Animation::interpolateKeyframes, "interpolate", "interpolate-hovered"),
    DELETE(Animation::deleteKeyframe, "trash", "trash-hovered");

    fun apply(context: Processor) {
        val current = context.animationHandler.currentAnimation?: return
        val useCurrent = this == COPY || this == DELETE && current.keyframes.size <= 1 ||
                         this == PASTE && context.animationHandler.copiedFrame.id == -1
        val animation = context.animationHandler.getAnimation(useCurrent)?: return
        action.invoke(animation)
    }

    fun getIcon(hovered: Boolean): BufferedImage {
        val path = if (hovered) hoveredPath else iconPath
        return BufferedImage("$SPRITE_PATH$path.png")
    }
}