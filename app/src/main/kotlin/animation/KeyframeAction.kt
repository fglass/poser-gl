package animation

import animation.command.*
import animation.command.impl.*
import render.RenderContext
import util.ResourceMap
import kotlin.reflect.KFunction1

enum class KeyframeAction(private val reference: KFunction1<RenderContext, Command>, key: String) {

    ADD(::AddKeyframeCommand, "add"),
    COPY(::CopyKeyframeCommand, "copy"),
    PASTE(::PasteKeyframeCommand, "paste"),
    INTERPOLATE(::LerpKeyframeCommand, "lerp"),
    DELETE(::DeleteKeyframeCommand, "trash");

    val icon = ResourceMap[key]
    val hoveredIcon = ResourceMap["$key-hovered"]

    fun apply(context: RenderContext) {
        val command = reference.invoke(context)
        context.animationHandler.executeCommand(command)
    }

    override fun toString() = name.toLowerCase().capitalize()
}