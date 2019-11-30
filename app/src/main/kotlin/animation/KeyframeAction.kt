package animation

import animation.command.*
import render.SPRITE_PATH
import render.RenderContext
import org.liquidengine.legui.image.BufferedImage
import kotlin.reflect.KFunction1

enum class KeyframeAction(private val commandReference: KFunction1<RenderContext, Command>, iconName: String,
                          hoveredIconName: String) {

    ADD(::AddKeyframeCommand, "add", "add-hovered"),
    COPY(::CopyKeyframeCommand, "copy", "copy-hovered"),
    PASTE(::PasteKeyframeCommand, "paste", "paste-hovered"),
    INTERPOLATE(::InterpolateKeyframeCommand, "interpolate", "interpolate-hovered"),
    DELETE(::DeleteKeyframeCommand, "trash", "trash-hovered");

    fun apply(context: RenderContext) {
        val command = commandReference.invoke(context)
        command.execute()
    }

    val icon = BufferedImage("$SPRITE_PATH$iconName.png")
    val hoveredIcon = BufferedImage("$SPRITE_PATH$hoveredIconName.png")
}