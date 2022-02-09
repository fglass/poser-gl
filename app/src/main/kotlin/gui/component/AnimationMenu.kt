package gui.component

import render.RenderContext
import org.joml.Vector2f
import com.spinyowl.legui.component.Panel
import com.spinyowl.legui.event.MouseClickEvent
import com.spinyowl.legui.input.Mouse
import com.spinyowl.legui.style.Style
import com.spinyowl.legui.style.color.ColorConstants
import com.spinyowl.legui.style.flex.FlexStyle
import util.ResourceMap
import util.setSizeLimits

class AnimationMenu(context: RenderContext): Panel() {

    private val play: ImageButton
    private val playIcon = ResourceMap["play"]
    private val playHoveredIcon = ResourceMap["play-hovered"]
    private val pauseIcon = ResourceMap["pause"]
    private val pauseHoveredIcon = ResourceMap["pause-hovered"]
    private val infoIcon = ResourceMap["info"]
    private val infoHoveredIcon = ResourceMap["info-hovered"]
    private val nodeIcon = ResourceMap["node"]
    private val nodeToggledIcon = ResourceMap["node-toggled"]

    init {
        style.display = Style.DisplayType.FLEX
        style.flexStyle.flexDirection = FlexStyle.FlexDirection.ROW
        style.flexStyle.flexGrow = 1
        style.position = Style.PositionType.RELATIVE
        style.setMaxHeight(23f)

        style.background.color = ColorConstants.darkGray()
        style.border.isEnabled = false
        style.setBorderRadius(0f)

        play = ImageButton(Vector2f(), playIcon, "Play")
        play.listenerMap.addListener(MouseClickEvent::class.java) { event ->
            if (event.action == MouseClickEvent.MouseClickAction.CLICK) {
                context.animationHandler.togglePlay()
            }
        }
        play.style.setMargin(4f, 0f, 0f, 10f)
        play.setSizeLimits(17f, 17f)
        add(play)

        val infoButton = ImageButton(Vector2f(), infoIcon, "Information")
        infoButton.hoveredIcon = infoHoveredIcon
        infoButton.setSizeLimits(22f, 21f)
        infoButton.style.setMargin(3f, 0f, 0f, 28f)
        infoButton.listenerMap.addListener(MouseClickEvent::class.java) { event ->
            if (event.button == Mouse.MouseButton.MOUSE_BUTTON_LEFT &&
                event.action == MouseClickEvent.MouseClickAction.CLICK) {
                val animation = context.animationHandler.currentAnimation?: return@addListener
                SequenceDialog(context, animation).display()
            }
        }
        add(infoButton)

        val nodeToggle = ImageButton(Vector2f(), nodeIcon, "Skeleton")
        nodeToggle.setSizeLimits(19f, 18f)
        nodeToggle.style.setMargin(3f, 0f, 0f, 51f)
        nodeToggle.style.setBorderRadius(1f)
        nodeToggle.listenerMap.addListener(MouseClickEvent::class.java) { event ->
            if (event.button == Mouse.MouseButton.MOUSE_BUTTON_LEFT &&
                event.action == MouseClickEvent.MouseClickAction.CLICK) {
                nodeToggle.setIconImage(if (nodeToggle.image == nodeToggledIcon) nodeIcon else nodeToggledIcon)
                context.nodeRenderer.toggle()
            }
        }
        add(nodeToggle)
    }

    fun updatePlayIcon(playing: Boolean) {
        if (playing) {
            play.setIconImage(pauseIcon)
            play.hoveredIcon = pauseHoveredIcon
            play.setTooltipText("Pause")
            play
        } else {
            play.setIconImage(playIcon)
            play.hoveredIcon = playHoveredIcon
            play.setTooltipText("Play")
        }
    }
}