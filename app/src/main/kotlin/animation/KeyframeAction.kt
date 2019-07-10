package animation

import SPRITE_PATH
import Processor
import org.liquidengine.legui.image.BufferedImage
import kotlin.reflect.KFunction1

enum class KeyframeAction(private val action: KFunction1<Animation, Unit>, private val iconPath: String) {

    ADD(Animation::addKeyframe, "add.png"),
    COPY(Animation::copyKeyframe, "copy.png"),
    PASTE(Animation::pasteKeyframe, "paste.png"),
    INTERPOLATE(Animation::interpolateKeyframes, "interpolate.png"),
    DELETE(Animation::deleteKeyframe, "trash.png");

    fun apply(context: Processor) {
        val current = context.animationHandler.currentAnimation?: return
        val useCurrent = this == COPY || this == DELETE && current.keyframes.size <= 1 ||
                         this == PASTE && context.animationHandler.copiedFrame.id == -1
        val animation = context.animationHandler.getAnimation(useCurrent)?: return
        action.invoke(animation)
    }

    fun getIcon(): BufferedImage {
        return BufferedImage(SPRITE_PATH + iconPath)
    }
}