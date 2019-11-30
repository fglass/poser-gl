package animation

import animation.command.*
import animation.command.impl.*
import render.SPRITE_PATH
import render.RenderContext
import org.liquidengine.legui.image.BufferedImage
import kotlin.reflect.KFunction1

enum class KeyframeAction(private val reference: KFunction1<RenderContext, Command>, iconName: String,
                          hoveredIconName: String) {

    ADD(::AddKeyframeCommand, "add", "add-hovered"),
    COPY(::CopyKeyframeCommand, "copy", "copy-hovered"),
    PASTE(::PasteKeyframeCommand, "paste", "paste-hovered"),
    INTERPOLATE(::LerpKeyframeCommand, "interpolate", "interpolate-hovered"),
    DELETE(::DeleteKeyframeCommand, "trash", "trash-hovered");

    fun apply(context: RenderContext) {
        val command = reference.invoke(context)
        if (command.execute()) {
            context.animationHandler.history.add(command)
        }
    }

    val icon = BufferedImage("$SPRITE_PATH$iconName.png")
    val hoveredIcon = BufferedImage("$SPRITE_PATH$hoveredIconName.png")
}